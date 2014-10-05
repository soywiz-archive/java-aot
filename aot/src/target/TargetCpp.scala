package target

import java.io.File
import java.nio.charset.Charset

import _root_.util.{ProcessUtils, FileBytes, SootUtils}
import soot._
import soot.jimple.{ArrayRef, Expr, Stmt}
import target.base.BaseMethodContext
import target.context.{BaseMethodContext, BaseClassContext}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.io.Source

class TargetCpp extends TargetBase {
  def run(outputExecutableFile:File): scala.Unit = {
    ProcessUtils.runAndRedirect(outputExecutableFile.getAbsolutePath, outputExecutableFile.getParentFile)
  }

  def doMethodWithBody(method:SootMethod, body:String): Unit = {
    val returnType = mangler.typeToStringRef(method.getReturnType)
    val mangledFullName = mangler.mangleFullName(method)
    val mangledBaseName = mangler.mangleBaseName(method)
    val params = (0 to method.getParameterCount - 1).map(index => mangler.typeToStringRef(method.getParameterType(index)) + " " + getParamName(index)).mkString(", ")

    def generateHeader() = {
      var declaration = ""
      declaration += mangler.visibility(method) + ": "
      if (method.isStatic) {
        declaration += "static "
      } else {
        declaration += "virtual "
      }
      declaration += s"$returnType $mangledBaseName($params)"
      if (method.isAbstract) {
        declaration += " = 0"
      }

      declaration += ";"

      declaration
    }

    def getParamName(index: Int) = s"p$index"

    s"$returnType $mangledFullName($params) { $body }"
  }

  def doClassHeader(context:BaseClassContext): String = {
    val clazz = context.clazz
    val native_framework = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "framework").asInstanceOf[String]
    val native_library = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "library").asInstanceOf[String]
    val cflags = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "cflags").asInstanceOf[String]
    val native_header = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "header").asInstanceOf[String]

    var declaration = ""
    declaration += "#ifndef " + mangler.mangleFullClassName(clazz.getName) + "_def\n"
    declaration += "#define " + mangler.mangleFullClassName(clazz.getName) + "_def\n"

    declaration += "#include \"types.h\"\n"
    if (native_header != null) {
      declaration += native_header + "\n"
    }
    if (clazz.hasSuperclass) {
      val res = mangler.mangle(clazz.getSuperclass)
      if (res != "java_lang_Object") {
        declaration += "#include \"" + res + ".h\"\n"
      }
    }

    for (res <- clazz.getInterfaces.asScala) {
      declaration += "#include \"" + mangler.mangle(res) + ".h\"\n"
    }
    declaration += "\n"

    for (rc <- referencedClasses) declaration += "class " + mangler.mangle(rc) + ";\n"
    declaration += "\n"

    declaration += "class " + mangler.mangle(clazz)
    val extendItems = new ListBuffer[String]
    if (clazz.hasSuperclass && !clazz.isInterface) extendItems.append("public " + mangler.mangle(clazz.getSuperclass))
    //if (clazz.hasSuperclass) extendItems.append("public " + mangler.mangle(clazz.getSuperclass))
    for (interface <- clazz.getInterfaces.asScala) extendItems.append("public " + mangler.mangle(interface))
    if (extendItems.nonEmpty) declaration += " : " + extendItems.mkString(", ")
    declaration += " {\n"

    for (field <- clazz.getFields.asScala) {
      declaration += mangler.visibility(field) + ": " + mangler.staticity(field) + " " + mangler.typeToStringRef(field.getType) + " " + mangler.mangle(field) + ";\n"
    }
    for (result <- results) declaration += result.declaration + "\n"

    if (staticConstructor != null) {
      declaration += "class __StaticInit { public: __StaticInit(); };\n"
      declaration += "private: static __StaticInit* __staticInit;\n"
    }

    declaration += "};\n"

    declaration += "#endif\n"

    declaration
  }

  def doClassBody(context:BaseClassContext): String = {
    var definition = ""

    //for (rc <- referencedClasses) declaration += "#include \"" + Mangling.mangle(rc) + ".h\"\n"

    definition += "#include \"" + mangler.mangleFullClassName(clazz.getName) + ".h\"\n"
    for (rc <- referencedClasses) {
      val res = mangler.mangle(rc)
      if (res != "java_lang_Object") {
        definition += "#include \"" + res + ".h\"\n"
      }
    }

    val mangledClassType = mangler.mangleClassName(clazz.getName)

    for (field <- clazz.getFields.asScala) {
      if (field.isStatic) {
        val isRefType = mangler.isRefType(field.getType)
        val mangledFieldType = mangler.typeToStringRef(field.getType)
        val mangledFieldName = mangler.mangle(field)
        if (isRefType) {
          definition += s"$mangledFieldType $mangledClassType::$mangledFieldName = $mangledFieldType(NULL);\n"
        } else {
          definition += s"$mangledFieldType $mangledClassType::$mangledFieldName = ($mangledFieldType)0;\n"
        }
      }
    }

    for (result <- results) {
      if (result.definition != null) {
        definition += result.definition + "\n"
      } else {
        definition += s"// Not implemented method: {$result.method} (abstract, interface or native)\n"
      }
    }

    if (staticConstructor != null) {
      definition += s"$mangledClassType::__StaticInit::__StaticInit() { $mangledClassType::__clinit__(); }\n"
      definition += s"$mangledClassType::__StaticInit* $mangledClassType::__staticInit = new $mangledClassType::__StaticInit();\n"
    }

    definition
  }

  override def doInstanceof(baseType:Type, checkType:Type, value:Value, context:BaseMethodContext): String = {
    doValue(value, context) + " instanceof " + mangler.typeToStringRef(checkType)
  }

  override def doStaticField(kind:SootClass, fieldName:String, context:BaseMethodContext): String = {
    mangler.mangle(kind) + "::" + fieldName
  }

  override def doBinop(kind:Type, left:Value, right:Value, op:String, context:BaseMethodContext): String = {
    var l = doValue(left, context)
    var r = doValue(right, context)
    if (mangler.isRefType(left.getType)) l = s"$l.get()"
    if (mangler.isRefType(right.getType)) r = s"$r.get()"
    op match {
      case "cmp" | "cmpl" | "cmpg" => s"$op($l, $r)"
      case _ =>
        s"$l $op $r"
    }
  }

  override def doCast(fromType:Type, toType:Type, value:Value, context:BaseMethodContext): String = {
    //"((" + mangler.typeToStringRef(toType) + ")" + doValue(value) + ")"
    if (mangler.isRefType(toType)) {
      "(std::dynamic_pointer_cast< " + mangler.typeToStringNoRef(toType) + " >(" + doValue(value, context) + "))"
    } else {
      "((" + mangler.typeToStringRef(toType) + ")" + doValue(value, context) + ")"
    }
  }

  override def doNew(kind:Type, context:BaseMethodContext): String = {
    val newType = mangler.typeToStringNoRef(kind)
    s"std::shared_ptr< $newType >(new $newType())"
  }

  override def doLocal(localName: String, context:BaseMethodContext): String = localName
  override def doParam(paramName: String, context:BaseMethodContext): String = paramName
  override def doInstanceField(instance: Value, fieldName: String, context:BaseMethodContext): String = doValue(instance, context) + "->" + fieldName

  // negate is - or ~
  override def doNegate(value:Value, context:BaseMethodContext):String = "-(" + doValue(value, context) + ")"
  override def doLength(value:Value, context:BaseMethodContext):String = "((" + doValue(value, context) + ")->size())"
  override def doStringLiteral(s: String, context:BaseMethodContext): String = "cstr_to_JavaString(L\"" + escapeString(s) + "\")"
  override def doVariableAllocation(kind:Type, name:String, context:BaseMethodContext):String = {
    mangler.typeToStringRef(kind) + " " + name + ";\n"
  }

  override def doNewArray(kind: Type, size: Value, context:BaseMethodContext): String = {
    "std::shared_ptr< " + mangler.typeToStringNoRef(kind) + " >(new " + mangler.typeToStringNoRef(kind) + "(" + doValue(size, context) + "))"
  }

  override def doNewMultiArray(kind: Type, values: Array[Value], context:BaseMethodContext): String = {
    "new " + mangler.typeToStringNoRef(kind) + values.map(i => "[" + doValue(i, context) + "]").mkString
  }

  override def doNop(context:BaseMethodContext): String = s";"
  override def doGoto(unit: Unit, context:BaseMethodContext): String = "goto " + context.labels(unit) + ";"
  override def doReturn(returnType: Type, returnValue: Value, context:BaseMethodContext): String = "return " + doCastIfNeeded(returnType, returnValue, context) + ";"
  override def doReturnVoid(context:BaseMethodContext) = "return;"
  override def doCaughtException(value: Type, context:BaseMethodContext): String = {
    //"((void*)(__caughtexception))"
    //"((" + mangler.typeToStringRef(t.getType) + ")(__caughtexception))"
    "__caughtexception"
  }

  override def doArrayAccess(value: Value, value1: Value, context:BaseMethodContext): String = {
    val base = doValue(value, context)
    val index = doValue(value1, context)
    s"$base->get($index)"
    //"(" + mangler.typeToStringRef(t.getType) + ")(void *)(" + doValue(t.getBase) + "->get(" + doValue(t.getIndex) + "))"
  }

  override def doIf(condition: Value, target: Stmt, context:BaseMethodContext): String = {
    s"if (" + doValue(condition, context) + ") { goto " + context.labels(target) + "; }"
  }

  override def doConstantClass(s: String, context:BaseMethodContext): String = "NULL /* ClassConstant:" + s + "*/"
  override def doConstantNull(context:BaseMethodContext): String = "std::shared_ptr<void*>(NULL)"
  override def doConstantInt(value: Int, context:BaseMethodContext): String = s"${value}"
  override def doConstantLong(value: Long, context:BaseMethodContext): String = s"${value}L"
  override def doConstantFloat(value: Float, context:BaseMethodContext): String = s"${value}f"
  override def doConstantDouble(value: Double, context:BaseMethodContext): String = s"${value}"
  override def doConstantString(value: String, context:BaseMethodContext): String = escapeString(value)

  override def doThrow(value: Value, context:BaseMethodContext): String = "throw(" + doValue(value, context) + ");"
  override def doLabel(labelName: String, context:BaseMethodContext): String = s"$labelName:; "
  override def doTryStart(context:BaseMethodContext):String = "try {\n"
  override def doCatchAndGoto(trapType: SootClass, labelName: String, context:BaseMethodContext): String = {
    "} catch (std::shared_ptr<" + mangler.mangle(trapType) + s"> __caughtexception__) { __caughtexception = __caughtexception__; goto $labelName; }\n"
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
    // l-value
    leftOp match {
      case s: ArrayRef =>
        val base = doValue(s.getBase, context)
        val index = doValue(s.getIndex, context)
        val right = doValue(rightOp, context)
        s"$base->set($index, $right);"
      case _ =>
        if (rightOp.getType.equals(leftOp.getType)) {
          doValue(leftOp, context) + " = " + doValue(rightOp, context) + ";"
        } else {
          // Exceptions for example!
          doValue(leftOp, context) + " = " + doCast(rightOp.getType, leftOp.getType, rightOp, context) + ";"
        }
    }
  }

  override def doExprStm(expr: Expr, context:BaseMethodContext): String = doExpr(expr, context) + ";"

  override def doThisRef(clazz: SootClass, context:BaseMethodContext): String = doWrapRefType(clazz, "this", context)

  override def doEnterMonitor(value: Value, context:BaseMethodContext): String = "RuntimeEnterMonitor(" + doValue(value, context) + ")"
  override def doExitMonitor(value: Value, context:BaseMethodContext): String = "RuntimeExitMonitor(" + doValue(value, context) + ")"

  override def doInvokeStatic(method: SootMethod, args: Seq[String], context:BaseMethodContext): String = mangler.mangleFullName(method) + "(" + args.mkString + ")"

  override def doInvokeInstance(base: Value, method: SootMethod, args: List[String], special:Boolean, context:BaseMethodContext): String = {
    val argsCall = args.mkString(", ")
    if (special) {
      doValue(base, context) + "->" + mangler.mangle(method.getDeclaringClass) + "::" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
    } else {
      if (method.getDeclaringClass.getName == "java.lang.Object") {
        // Required for interfaces not extending Object directly
        "((std::dynamic_pointer_cast<java_lang_Object>)(" + doValue(base, context) + "))->" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
      } else {
        doValue(base, context) + "->" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
      }
    }
  }

  private def doWrapRefType(toType:SootClass, value: String, context:BaseMethodContext): String = {
    "std::shared_ptr< " + mangler.mangle(toType) + " >(" + value + ")"
  }

  private def doWrapRefType(toType:Type, value: String, context:BaseMethodContext): String = {
    "std::shared_ptr< " + mangler.typeToStringNoRef(toType) + " >(" + value + ")"
  }

  private def escapeString(str: String): String = {
    str.map(_ match {
      case '"' => "\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => _
    }).mkString("")
  }

  object mangler extends BaseMangler {
    def mangle(clazz:SootClass): String = mangleClassName(clazz.getName)
    def mangle(field:SootField): String = field.getName
    def mangleClassName(name:String):String = name.replace('.', '_')
    def mangleFullClassName(name:String):String = name.replace('.', '_')

    //override def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "protected" else "private"
    def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "public" else "public"
    def staticity(member:ClassMember):String = if (member.isStatic) "static" else ""

    def typeToStringRef(kind:Type): String = {
      kind match {
        case r:RefType => "std::shared_ptr< " + typeToStringNoRef(kind) + " >"
        case r:ArrayType => "std::shared_ptr< " + typeToStringNoRef(kind) + " >"
        case _ => typeToStringNoRef(kind)
      }
    }

    def typeToStringNoRef(kind:Type): String = {
      kind match {
        case v:VoidType => "void"
        case v:NullType => "NULL"
        case prim:PrimType =>
          prim match {
            case v:BooleanType => "bool"
            case v:ByteType => "int8"
            case v:CharType => "wchar_t"
            case v:ShortType => "int16"
            case v:IntType => "int32"
            case v:LongType => "int64"
            case v:FloatType => "float32"
            case v:DoubleType => "float64"
          }
        //case r:ArrayType if r.getElementType.isInstanceOf[RefType] => "Array<java_lang_Object*>"
        case r:ArrayType => "Array< " + typeToStringRef(r.getElementType) + " >"
        case r:RefType => mangleClassName(r.getClassName)
      }
    }
  }

  object CppGeneratorBuildMacOS {
    def createAPP(path:String, name:String, executable:Array[Byte], png512x512:Array[Byte]): scala.Unit = {
      val utf8 = Charset.forName("UTF-8")
      new File(s"$path/Contents/Resources").mkdirs()
      new File(s"$path/Contents/MacOS").mkdirs()
      new File(s"$path/Contents/Frameworks").mkdirs()
      FileBytes.write(new File(s"$path/Contents/MacOS/app"), executable)
      Runtime.getRuntime.exec(s"chmod +x $path/Contents/MacOS/app")
      Runtime.getRuntime.exec(s"strip $path/Contents/MacOS/app")

      FileBytes.write(new File(s"$path/Contents/Resources/app.icns"), createIcns(png512x512))
      FileBytes.write(new File(s"$path/Contents/PkgInfo"), utf8, "APPL????")
      FileBytes.write(new File(s"$path/Contents/Info.plist"), utf8,
        s"""
      |<?xml version="1.0" encoding="UTF-8"?>
      |<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
      |<plist version="1.0">
      |<dict>
      |	<key>CFBundleExecutable</key><string>app</string>
      |	<key>CFBundleGetInfoString</key><string>0.1, 2014-Jul-08, App Foundation</string>
      |	<key>CFBundleIconFile</key><string>app.icns</string>
      |	<key>CFBundleIdentifier</key><string>com.soywiz.sample1</string>
      |	<key>CFBundleInfoDictionaryVersion</key><string>1.0</string>
      |	<key>CFBundleName</key><string>$name</string>
      |	<key>CFBundlePackageType</key><string>APPL</string>
      |	<key>CFBundleShortVersionString</key><string>1.0</string>
      |	<key>CFBundleSignature</key><string>????</string>
      |	<key>CFBundleVersion</key><string>0.1, 2014-Jul-08, App Foundation</string>
      |	<key>NSPrincipalClass</key><string>NSApplication</string>
      |	<key>NSHighResolutionCapable</key><true/>
      |</dict>
      |</plist>
    """.stripMargin
      )
    }

    private def createIcns(png512x512:Array[Byte]) = {
      val outPath = System.getProperty("java.io.tmpdir") + "/build_iconset"
      val outPathSet = s"$outPath/app.iconset"
      new File(outPathSet).mkdirs()
      FileBytes.write(new File(s"$outPath/app.png"), png512x512)

      def exec(command:String) = {
        println(command)
        val p = Runtime.getRuntime.exec(command)
        for (line <- Source.fromInputStream(p.getInputStream).getLines()) println(line)
        for (line <- Source.fromInputStream(p.getErrorStream).getLines()) println(line)
        p.waitFor()
      }

      for (size <- List(16, 256)) exec(s"sips -z $size $size $outPath/app.png --out $outPathSet/icon_${size}x${size}.png")
      exec(s"iconutil -c icns -o $outPath/app.icns $outPathSet")
      FileBytes.read(new File(s"$outPath/app.icns"))
    }
  }
}
