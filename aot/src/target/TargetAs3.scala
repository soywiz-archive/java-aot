package target

import java.io.File
import java.nio.charset.Charset

import _root_.util._
import com.sun.javaws.exceptions.InvalidArgumentException
import soot._
import soot.jimple._
import vfs.VfsNode

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

// /Developer/airsdk15/bin/mxmlc -optimize=true -debug=false -verbose-stacktraces=false -inline  +configname=air -source-path+=. BootMain.as -output=a.swf
class TargetAs3 extends Target {
  val targetName = "as3"

  override def generateProject(context: BaseProjectContext): scala.Unit = {
    super.generateProject(context)

    val runtimeVfs = context.runtime.runtimeClassesVfs
    val outputVfs = context.output
    runtimeVfs.access("as3").copyTreeTo(outputVfs)

    context.bootImports.append("import " + mangler.getRealClassName(context.mainClass) + ";");

    val bootMainString = runtimeVfs.access("as3/BootMain.as").read(utf8)
      .replace("/*!IMPORTS*/", context.bootImports.mkString("\n"))
      .replace("/*!PREINIT*/", context.preInitLines.mkString("\n"))
      .replace("/*!CALLMAIN*/", mangler.getRealClassName(context.mainClass) + ".main_java_lang_String$Array(args)")

    for (key <- mangler.abstractTypes.keys) {
      outputVfs.access(key.replace(".", "/") + ".as").remove()
    }

    outputVfs.access("BootMain.as").write(bootMainString, utf8)
    outputVfs.access("build.sh").write("/Developer/airsdk15/bin/mxmlc -optimize=false -debug=true +configname=air -source-path+=. BootMain.as -output=a.swf", utf8)
    outputVfs.access("build_release.sh").write("/Developer/airsdk15/bin/mxmlc -optimize=true -debug=false -inline=true +configname=air -source-path+=. BootMain.as -output=a.swf", utf8)
    outputVfs.access("build.bat").write("@c:\\dev\\airsdk15\\bin\\mxmlc -optimize=false -debug=true +configname=air -source-path+=. BootMain.as -output=a.swf", utf8)
  }

  override def generateClass(clazz: BaseClassContext): scala.Unit = {
    super.generateClass(clazz)
    val outputPath = clazz.projectContext.output
    val body = doClassBody(clazz)
    val classPath = classNameToPath(clazz.clazz.getName)
    outputPath.access(s"$classPath.as").ensureParentPath().write(body, utf8)
  }

  override protected def classNameToPath(name:String): String = mangler.getClassPath(name)

  override def createClassContext(projectContext:BaseProjectContext, clazz:TargetClass): BaseClassContext = {
    new As3ClassContext(projectContext, clazz)
  }

  override def createProjectContext(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode): BaseProjectContext = {
    new As3ProjectContext(classNames, mainClass, runtimeProvider, outputPath)
  }

  class As3ClassContext(projectContext:BaseProjectContext, clazz:TargetClass) extends BaseClassContext(projectContext, clazz) {
  }
  
  class As3ProjectContext(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode)  extends BaseProjectContext(classNames, mainClass, runtimeProvider, outputPath) {
  }

  override def buildProject(projectContext: BaseProjectContext): scala.Unit = {
  }

  def getOutputExecutablePath(projectContext:BaseProjectContext): VfsNode = projectContext.output.access("a.swf")

  override def runProject(projectContext:BaseProjectContext): scala.Unit = {
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def doMethodDefinitionHead(context:BaseMethodContext): String = {
    val method = context.method
    val returnType = mangler.typeToStringRef(method.getReturnType)
    val mangledFullName = mangler.mangleMethodName(method)
    val params = getMethodParams(method)
    val static = if (method.isStatic) "static" else if (mustOverrideMethod(method)) "override" else ""
    s"$static public function $mangledFullName($params):$returnType"
  }

  def mustOverrideMethod(method:SootMethod):Boolean = {
    SootUtils.isMethodOverriding(method) && !mangler.abstractTypes.contains(method.getDeclaringClass.getSuperclass.getName)
  }

  def getParamName(index: Int) = s"p$index"

  def getMethodParams(method:SootMethod): String = {
    (0 until method.getParameterCount).map(index => getParamName(index) + ":" + mangler.typeToStringRef(method.getParameterType(index))).mkString(", ")
  }

  override def doMethodWithBody(context:BaseMethodContext): String = {
    val head = doMethodDefinitionHead(context)
    val body = doMethodBody(context)
    s"$head { $body }"
  }

  def getAs3FileForClass(className:String):String = classNameToPath(className) + ".as"
  def getAs3FileForClass(clazz:SootClass):String = getAs3FileForClass(clazz.getName)

  def doClassBody(context:BaseClassContext): String = {
    val clazz = context.clazz
    val referencedClasses = context.referencedClasses
    var definition = ""

    definition += "package " + clazz.getName.split('.').dropRight(1).mkString(".") + " {\n"

    //for (rc <- referencedClasses) declaration += "#include \"" + Mangling.mangle(rc) + ".h\"\n"

    val classesToInclude = new ListBuffer[SootClass]()

    classesToInclude.append(clazz)
    for (rc <- referencedClasses) classesToInclude.append(rc)

    for (res <- classesToInclude) {
      if (!mangler.abstractTypes.contains(res.getName)) {
        definition += "import " + mangler.getRealClassName(res) + ";\n"
      } else {
        val (as3, staticType) = mangler.abstractTypes(res.getName)
        definition += "import " + staticType + ";\n"
      }
    }

    val typeKind = if (clazz.isInterface) "interface" else "class"
    val extendingList = new ListBuffer[String]
    val implementingList = new ListBuffer[String]

    val interfaceList = clazz.getInterfaces.asScala.map(_.getName)

    if (clazz.isInterface) {
      extendingList.appendAll(interfaceList)
    } else {
      //if (clazz.hasSuperclass && clazz.getSuperclass.getName != "java.lang.Object") extendingList.append(clazz.getSuperclass.getName)
      if (clazz.hasSuperclass) extendingList.append(clazz.getSuperclass.getName)
      implementingList.appendAll(interfaceList)
    }

    definition += s"public $typeKind " + mangler.getShortName(clazz)
    if (extendingList.nonEmpty) definition += " extends " + extendingList.map(mangler.mangleClassName).mkString(", ")
    if (implementingList.nonEmpty) definition += " implements " + implementingList.map(mangler.mangleClassName).mkString(", ")
    definition += " {\n"

    val mangledClassType = mangler.mangleClassName(clazz.getName)

    for (field <- clazz.getFields.asScala) {
      val mangledFieldType = mangler.typeToStringRef(field.getType)
      val mangledFieldName = mangler.mangle(field)
      val nullValue = field.getType match {
        case _:RefLikeType => "null"
        case _:BooleanType => "false"
        case _ => "0"
      }
      val static = if (field.isStatic) "static" else ""
      definition += s"$static public var $mangledFieldName:$mangledFieldType = $nullValue;\n"
    }

    val methodSignatures = new mutable.HashSet[String]()
    def getMethodSignature(method:SootMethod):String = method.getName + ":" + method.getParameterTypes.asScala.mkString(",") + method.getReturnType.toString()

    for (result <- context.methods) {
      val method = result.method

      methodSignatures.add(getMethodSignature(method))

      if (result.methodWithBody != null) {
        definition += result.methodWithBody + "\n"
      } else {
        if (context.clazz.isInterface && method.isAbstract) {
          definition += "function " + mangler.mangleMethodName(method) + "(" + getMethodParams(method) + "):" + mangler.typeToStringRef(method.getReturnType) + ";\n"
        } else {
          if (method.isAbstract) {
            definition += "public function " + mangler.mangleMethodName(method) + "(" + getMethodParams(method) + "):" + mangler.typeToStringRef(method.getReturnType) + " { throw(new Error(\"Abstract\")); }\n"
          } else {
            definition += s"// Not implemented method: ${mangler.mangleMethodName(method)} (abstract, interface or native)\n"
          }
        }
      }
    }

    // Class is abstract, and as3 is not supporting abstract classes, so we should locate not declared methods
    if (clazz.isAbstract && !clazz.isInterface) {


      //println("Class:" + clazz.getName)
      for (interface <- SootUtils.getAllDirectInterfaces(clazz).distinct) {
        //println("  Interface:" + interface.getName)
        for (method <- interface.getMethods.asScala) {
          val methodSignature = getMethodSignature(method)
          if (!methodSignatures.contains(methodSignature)) {
            methodSignatures.add(methodSignature)
            definition += "public function " + mangler.mangleMethodName(method) + "(" + getMethodParams(method) + "):" + mangler.typeToStringRef(method.getReturnType) + " { throw(new Error(\"Interface Abstract\")); }\n"
          }
        }
      }
    }

    if (context.hasStaticConstructor) {
      context.projectContext.bootImports.append(s"import $mangledClassType;")
      context.projectContext.preInitLines.append(s"\t$mangledClassType.__clinit__();")
    }

    // Class
    definition += "}\n"

    // Package
    definition += "}\n"

    definition
  }

  override def doInstanceof(baseType:Type, checkType:Type, value:Value, context:BaseMethodContext): String = {
    doValue(value, context) + " instanceof " + mangler.typeToStringRef(checkType)
  }

  override def doStaticField(kind:SootClass, fieldName:String, context:BaseMethodContext): String = {
    mangler.mangle(kind) + "." + fieldName
  }

  override def doBinop(resultKind:Type, left:Value, right:Value, op:String, context:BaseMethodContext): String = {
    var l = doValue(left, context)
    var r = doValue(right, context)
    //if (mangler.isRefType(left.getType)) l = s"$l.get()"
    //if (mangler.isRefType(right.getType)) r = s"$r.get()"

    left.getType match {
      case vv:LongType =>
        op match {
          case "cmp" => s"Long.cmp($l, $r)"

          case "+" => s"Long.add($l, $r)"
          case "-" => s"Long.sub($l, $r)"

          case "*" => s"Long.mul($l, $r)"
          case "/" => s"Long.div($l, $r)"
          case "%" => s"Long.mod($l, $r)"

          case "&" => s"Long.and($l, $r)"
          case "|" => s"Long.or($l, $r)"
          case "^" => s"Long.xor($l, $r)"

          case "<<" => s"Long.shl($l, $r)"
          case ">>" => s"Long.sar($l, $r)"
          case ">>>" => s"Long.shr($l, $r)"

          case "==" => s"Long.eq($l, $r)"
          case "!=" => s"Long.ne($l, $r)"
          case ">=" => s"Long.ge($l, $r)"
          case "<=" => s"Long.le($l, $r)"
          case ">" => s"Long.gt($l, $r)"
          case "<" => s"Long.lt($l, $r)"
        }
      case vv:DoubleType =>
        op match {
          case "cmp" | "cmpl" | "cmpg" => s"RuntimeTools.number_$op($l, $r)"
          case _ => s"$l $op $r"
        }

      case vv:FloatType =>
        op match {
          case "cmp" | "cmpl" | "cmpg" => s"RuntimeTools.number_$op($l, $r)"
          case _ => s"$l $op $r"
        }

      case _ =>
        s"$l $op $r"
    }
  }

  def registerAbstract(javaType:String, as3Type:String, as3StaticMethodType:String): scala.Unit ={

  }

  registerAbstract("java.lang.Object", "Object", "ObjectTools")
  registerAbstract("java.lang.String", "String", "StringTools")

  override def doCast(fromType:Type, toType:Type, value:Value, context:BaseMethodContext): String = {
    val valueStr = doValue(value, context)
    val toCastTypeStr = mangler.typeToStringRef(toType)

    if (fromType == toType) {
      s"$valueStr"
    } else {
      val processedValueStr = fromType match {
        case e:NullType => s"$valueStr"
        case e:BooleanType => s"(($valueStr) ? 1 : 0)"
        case e:ByteType => s"$valueStr"
        case e:CharType => s"$valueStr"
        case e:ShortType => s"$valueStr"
        case e:IntType => s"$valueStr"
        case e:FloatType => s"$valueStr"
        case e:DoubleType => s"$valueStr"
        case e:RefType => s"$valueStr"
        case e:ArrayType => s"$valueStr"
        case e:LongType => s"(($valueStr).toInt())"
      }

      toType match {
        case e:BooleanType => s"$processedValueStr != 0"
        case e:ByteType => s"RuntimeTools.toByte($processedValueStr)"
        case e:ShortType => s"RuntimeTools.toShort($processedValueStr)"
        case e:CharType => s"($processedValueStr & 0xFFFF)"
        case e:IntType => s"$processedValueStr"
        case e:FloatType =>s"Number($processedValueStr)"
        case e:DoubleType => s"Number($processedValueStr)"
        case e:RefType => s"($valueStr as $toCastTypeStr)"
        case e:ArrayType => s"($valueStr as $toCastTypeStr)"
        case e:LongType =>
          fromType match {
            case e:FloatType => s"Long.fromNumber($processedValueStr)"
            case e:DoubleType => s"Long.fromNumber($processedValueStr)"
            case _ => s"Long.fromInt($processedValueStr)"
          }
      }
    }
  }

  override def doNew(kind:Type, context:BaseMethodContext): String = {
    val newType = mangler.typeToStringNoRef(kind)
    s"new $newType()"
  }

  override def doLocal(localName: String, context:BaseMethodContext): String = localName
  override def doParam(paramName: String, context:BaseMethodContext): String = paramName
  override def doInstanceField(instance: Value, fieldName: String, context:BaseMethodContext): String = doValue(instance, context) + "." + fieldName

  // negate is - or ~
  override def doNegate(value:Value, context:BaseMethodContext):String = {
    value.getType match {
      case l:LongType => "Long.neg(" + doValue(value, context) + ")"
      case _ => "-(" + doValue(value, context) + ")"
    }

  }
  override def doLength(value:Value, context:BaseMethodContext):String = "((" + doValue(value, context) + ").length)"
  override def doVariableAllocation(kind:Type, name:String, context:BaseMethodContext):String = {
    "var " + name + ":" + mangler.typeToStringRef(kind) + ";\n"
  }

  override def doNewArray(kind: Type, size: Value, context:BaseMethodContext): String = "new Array(" + doValue(size, context) + ")"
  override def doNewMultiArray(kind: Type, sizes: Array[Value], context:BaseMethodContext): String = "new " + mangler.typeToStringNoRef(kind) + sizes.map(i => "[" + doValue(i, context) + "]").mkString
  override def doNop(context:BaseMethodContext): String = s";"
  override def doGoto(unit: Unit, context:BaseMethodContext): String = "goto " + context.labels(unit) + ";"
  override def doReturn(returnType: Type, returnValue: Value, context:BaseMethodContext): String = "return " + doCastIfNeeded(returnType, returnValue, context) + ";"
  override def doReturnVoid(context:BaseMethodContext) = "return;"
  override def doCaughtException(value: Type, context:BaseMethodContext): String = "__caughtexception"
  override def doArrayAccess(value: Value, value1: Value, context:BaseMethodContext): String = {
    val base = doValue(value, context)
    val index = doValue(value1, context)
    s"$base[$index]"
  }

  override def doIf(condition: Value, target: Stmt, context:BaseMethodContext): String = s"if (" + doValue(condition, context) + ") { goto " + context.labels(target) + "; }"
  override def doConstantClass(s: String, context:BaseMethodContext): String = "null /* ClassConstant:" + s + "*/"
  override def doConstantNull(context:BaseMethodContext): String = "null"
  override def doConstantInt(value: Int, context:BaseMethodContext): String = s"$value"
  override def doConstantLong(value: Long, context:BaseMethodContext): String = s"Long.value(" + (value & 0xFFFFFFFFL) + ", " + (value >> 32) + ")"
  override def doConstantFloat(value: Float, context:BaseMethodContext): String = s"$value"
  override def doConstantDouble(value: Double, context:BaseMethodContext): String = s"$value"
  override def doConstantString(value: String, context:BaseMethodContext): String = "\"" + escapeString(value) + "\""

  override def doThrow(value: Value, context:BaseMethodContext): String = "throw(" + doValue(value, context) + ");"
  override def doLabel(labelName: String, context:BaseMethodContext): String = s"$labelName:; "
  override def doTryStart(context:BaseMethodContext):String = "try {\n"
  override def doCatchAndGoto(trapType: SootClass, labelName: String, context:BaseMethodContext): String = {
    "} catch (__caughtexception__:" + mangler.mangle(trapType) + s") { __caughtexception = __caughtexception__; goto $labelName; }\n"
  }
  override def doSwitch(matchValue:Value, defaultLabel: String, map: scala.collection.mutable.HashMap[Int, String], context:BaseMethodContext): String = {
    def matchValueString = doValue(matchValue, context)
    def defaultCase = s"default: goto $defaultLabel; break;\n"
    def cases = {
      map.map(v => {
        val(key, label) = v
        s"case $key: goto $label; break;\n"
      }).mkString
    }

    s"switch ($matchValueString) { $cases $defaultCase }"
  }

  override def doAssign(leftOp: Value, rightOp: Value, context:BaseMethodContext): String = {
    doValue(leftOp, context) + " = " + doCastIfNeeded(leftOp.getType, rightOp, context) + ";"
  }

  override def doExprStm(expr: Expr, context:BaseMethodContext): String = doExpr(expr, context) + ";"
  override def doThisRef(clazz: SootClass, context:BaseMethodContext): String = "this"
  override def doEnterMonitor(value: Value, context:BaseMethodContext): String = "//RuntimeEnterMonitor(" + doValue(value, context) + ")"
  override def doExitMonitor(value: Value, context:BaseMethodContext): String = "//RuntimeExitMonitor(" + doValue(value, context) + ")"

  override def doInvokeStatic(method: SootMethod, args: Seq[String], context:BaseMethodContext): String = mangler.mangleFullMethodName(method) + "(" + args.mkString(", ") + ")"

  override def doInvokeInstance(base: Value, method: SootMethod, args: List[String], special:Boolean, context:BaseMethodContext): String = {
    val argsCall = args.mkString(", ")
    if (special) {
      //doValue(base, context) + "." + mangler.mangle(method.getDeclaringClass) + "::" + mangler.mangleMethodName(method) + "(" + argsCall + ")"
      val instanceSootClass = base.getType.asInstanceOf[RefType].getSootClass

      if (instanceSootClass.equals(method.getDeclaringClass)) {
        if (mangler.abstractTypes.contains(method.getDeclaringClass.getName)) {
          val (as3, staticType) = mangler.abstractTypes(method.getDeclaringClass.getName)
          doValue(base, context) + " = " + staticType + "." + mangler.mangleMethodName(method) + "(" + (args).mkString(", ") + ")"
        } else {
          doValue(base, context) + "." + mangler.mangleMethodName(method) + "(" + argsCall + ")"
        }
      } else {
        if (mangler.abstractTypes.contains(method.getDeclaringClass.getName)) {
          // Nothing
          ""
        } else if (instanceSootClass.hasSuperclass && instanceSootClass.getSuperclass.equals(method.getDeclaringClass)) {
          //"/* WARNING: USING SUPER DIRECTLY! CHECK! */ super." +
          "super." + mangler.mangleMethodName(method) + "(" + argsCall + ")"
        } else {
          throw new InvalidArgumentException(Array("Calling special without super"))
        }
      }
    } else {
      if (mangler.abstractTypes.contains(method.getDeclaringClass.getName)) {
        val (as3, staticType) = mangler.abstractTypes(method.getDeclaringClass.getName)
        staticType + "." + mangler.mangleMethodName(method) + "(" + (doValue(base, context) :: args).mkString(", ") + ")"
      } else {
        doValue(base, context) + "." + mangler.mangleMethodName(method) + "(" + argsCall + ")"
      }
    }
  }

  private def escapeString(str: String): String = {
    str.map(c => c match {
      case '"' => "\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => c
    }).mkString("")
  }

  val mangler = As3Mangler

  object As3Mangler extends BaseMangler {
    val abstractTypes = new mutable.HashMap[String, (String, String)]()

    def declareAbstractType(javaType:String, as3Type:String, staticMethodsType:String): scala.Unit = {
      abstractTypes(javaType) = (as3Type, staticMethodsType)
    }

    declareAbstractType("java.lang.Object", "Object", "ObjectImpl")
    declareAbstractType("java.lang.String", "String", "StringImpl")

    def mangle(clazz:SootClass): String = mangleClassName(clazz.getName)
    def mangle(field:SootField): String = field.getName
    def mangleClassName(name:String):String = {
      if (abstractTypes.contains(name)) {
        abstractTypes(name)._1
      } else {
        getRealClassName(name)
      }
    }

    def getRealClassName(clazz:SootClass):String = getRealClassName(clazz.getName)

    def getRealClassName(name:String):String = {
      val chunks = name.split('.')
      val packageName = chunks.dropRight(1).mkString(".")
      val baseClassName = _getShortName(chunks.last)
      if (packageName.isEmpty) {
        baseClassName
      } else {
        s"$packageName.$baseClassName"
      }
    }

    def getShortName(clazz:SootClass):String = _getShortName(clazz.getShortName)
    private def _getShortName(shortName:String):String = {
      shortName match {
        case "Number" | "Object" | "Class" => s"_$shortName"
        case _ => shortName
      }
    }

    def getClassPath(name:String):String = {
      getRealClassName(name).replace(".", "/")
    }

    def mangleFullClassName(name:String):String = mangleClassName(name)
    //override def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "protected" else "private"
    def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "public" else "public"
    def staticity(member:ClassMember):String = if (member.isStatic) "static" else ""

    def typeToStringRef(kind:Type): String = typeToStringNoRef(kind)

    def typeToStringNoRef(kind:Type): String = {
      kind match {
        case v:VoidType => "void"
        case v:NullType => "null"
        case v:BooleanType => "Boolean"
        case v:ByteType => "int"
        case v:CharType => "int"
        case v:ShortType => "int"
        case v:IntType => "int"
        case v:LongType => "Long"
        case v:FloatType => "Number"
        case v:DoubleType => "Number"
        //case r:ArrayType if r.getElementType.isInstanceOf[RefType] => "Array<java_lang_Object*>"
        case r:ArrayType => "Array"
        case r:RefType => mangleClassName(r.getClassName)
      }
    }

    override def mangleMethodName(method: SootMethod): String = {
      if (method.getParameterCount == 0) {
        escapeSpecialChars(method.getName)
      } else {
        escapeSpecialChars(method.getName + "_" + method.getParameterTypes.asScala.map(_.asInstanceOf[Type].toString).mkString("_"))
      }
    }

    override def mangleFullMethodName(method: SootMethod): String = mangleClassName(method.getDeclaringClass.getName) + "." + mangleMethodName(method)
  }
}
