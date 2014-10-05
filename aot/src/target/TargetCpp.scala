package target

import java.io.File
import java.nio.charset.Charset

import _root_.util._
import soot._
import soot.jimple.{ArrayRef, Expr, Stmt}
import vfs.{FileVfsNode, VfsNode}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

class TargetCpp extends TargetBase {
  override def generateProject(context: BaseProjectContext): scala.Unit = {
    val runtimeVfs = context.runtime.runtimeClassesVfs
    val outputVfs = context.output
    runtimeVfs.access("cpp").copyTreeTo(outputVfs)

    super.generateProject(context)
  }

  override def generateClass(clazz: BaseClassContext): scala.Unit = {
    super.generateClass(clazz)
    val outputPath = clazz.projectContext.output
    val body = doClassBody(clazz)
    val head = doClassHeader(clazz)
    val classPath = classNameToPath(clazz.clazz.getName)
    outputPath.access(s"$classPath.h").ensureParentPath().write(head, utf8)
    outputPath.access(s"$classPath.cpp").ensureParentPath().write(body, utf8)
  }

  override def createClassContext(projectContext:BaseProjectContext, clazz:SootClass): BaseClassContext = {
    new CppClassContext(projectContext, clazz)
  }

  override def createProjectContext(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode): BaseProjectContext = {
    new CppProjectContext(classNames, mainClass, runtimeProvider, outputPath)
  }

  class CppClassContext(projectContext:BaseProjectContext, clazz:SootClass) extends BaseClassContext(projectContext, clazz) {
    var native_header = ""
    def cppProject = projectContext.asInstanceOf[CppProjectContext]
  }
  
  class CppProjectContext(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode)  extends BaseProjectContext(classNames, mainClass, runtimeProvider, outputPath) {
    var frameworks = new mutable.HashSet[String]
    var libraries = new mutable.HashSet[String]
    var cflags = new mutable.HashSet[String]
  }

  override def buildProject(projectContext: BaseProjectContext): scala.Unit = {
    val cppProjectContext = projectContext.asInstanceOf[CppProjectContext]
    
    val runtime = projectContext.runtime
    var java_macos_embedded_frameworks = runtime.java_sample1_classes_path + "/frameworks/cpp"
    if (OS.isWindows) java_macos_embedded_frameworks = "^/+".r.replaceAllIn(java_macos_embedded_frameworks, "")

    val mainClassName = mangler.mangleClassName(projectContext.mainClass)

    def createMain(): String = {
      val main_cpp = runtime.runtimeClassesVfs.access("cpp/main.cpp").read(utf8)
      s"#define __ENTRY_POINT_METHOD__ $mainClassName::main\n$main_cpp"
    }

    val outputPath = projectContext.output

    outputPath.access("main.cpp").ensureParentPath().write(createMain(), utf8)
    val paths = projectContext.classNames.filter(_ != "java.lang.Object").map(name => classNameToPath(name) + ".cpp").mkString(" ")

    var frameworksAppend = ""
    if (OS.isMac) {
      frameworksAppend = cppProjectContext.frameworks.map(framework => {
        var result: String = null
        for (frameworkPath <- List(s"/Library/Frameworks/$framework.framework", s"/System/Library/Frameworks/$framework.framework")) {
          println(frameworkPath)
          if (new File(frameworkPath).exists()) {
            result = s"-I$frameworkPath/Versions/A/Headers -framework $framework"
          }
        }
        println(result)
        if (result == null) throw new Exception(s"Can't find framework $framework")
        result
      }).mkString(" ")
    }

    var libSystem = "MacOS"
    if (OS.isWindows) libSystem = "Windows"

    val libraryAppend = cppProjectContext.libraries.map(library => {
      var result: String = null
      for (libraryPath <- List(s"$java_macos_embedded_frameworks/$library")) {
        println(libraryPath)
        if (new File(libraryPath).exists()) {
          result = s"-I$libraryPath/include/$libSystem $libraryPath/lib/$libSystem/lib$library.a"
        }
      }
      println(result)
      if (result == null) throw new Exception(s"Can't find library $library")
      result
    }).mkString(" ")


    val cflagsAppend = cppProjectContext.cflags.mkString(" ")
    var command = s"g++ -fpermissive -Wint-to-pointer-cast "
    command += " \"-I" + outputPath.absoluteFullPath + "\""
    command += s" -O2 types.cpp main.cpp $paths $frameworksAppend $libraryAppend $cflagsAppend"
    if (OS.isMac) {
      command += s" -framework Cocoa -framework CoreAudio -F/Library/Frameworks -F$java_macos_embedded_frameworks"
      command += s" -D_THREAD_SAFE -lm -liconv -Wl,-framework,OpenGL -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-framework,CoreAudio -Wl,-framework,AudioToolbox -Wl,-framework,AudioUnit"
    }
    if (OS.isWindows) {
      command += s" -static-libstdc++ -static-libgcc -L. -lopengl32 -lshell32 -luser32 -lgdi32 -lwinmm -limm32 -lole32 -lkernel32 -lversion -lOleAut32 -lstdc++"
    }

    val outputExecutableFile = getOutputExecutablePath(projectContext)
    println(command)
    outputExecutableFile.remove()

    projectContext.output.access("build.bat").write(command, utf8)
    projectContext.output.access("build.sh").write(command, utf8)

    val result = ProcessUtils.runAndRedirect(command, new File(projectContext.output.absoluteFullPath)) == 0
    if (!result) throw new Exception("error building")

    if (OS.isMac) {
      val png512 = new FileVfsNode(runtime.java_runtime_classes_path).access("emptyicon.png").read()
      CppGeneratorBuildMacOS.createAPP(projectContext.output.access("test.app"), "sampleapp", outputExecutableFile.read(), png512)
    }
  }

  def getOutputExecutablePath(projectContext:BaseProjectContext): VfsNode = projectContext.output.access("a.out")

  override def runProject(projectContext:BaseProjectContext): scala.Unit = {
    val executable = getOutputExecutablePath(projectContext)
    ProcessUtils.runAndRedirect(executable.absoluteFullPath, new File(executable.parent.absoluteFullPath))
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def doMethodDeclaration(context:BaseMethodContext): String = {
    val method = context.method
    val returnType = mangler.typeToStringRef(method.getReturnType)
    val mangledBaseName = mangler.mangleBaseName(method)
    val params = getMethodParams(method)

    val visibility = mangler.visibility(method) + ": "
    val staticVirtual = if (method.isStatic) "static " else "virtual "
    val suffix = if (method.isAbstract) " = 0" else ""
    s"$visibility $staticVirtual $returnType $mangledBaseName($params) $suffix;"
  }

  def doMethodDefinitionHead(context:BaseMethodContext): String = {
    val method = context.method
    val returnType = mangler.typeToStringRef(method.getReturnType)
    val mangledFullName = mangler.mangleFullName(method)
    val params = getMethodParams(method)
    s"$returnType $mangledFullName($params)"
  }

  def getParamName(index: Int) = s"p$index"

  def getMethodParams(method:SootMethod): String = {
    (0 until method.getParameterCount).map(index => mangler.typeToStringRef(method.getParameterType(index)) + " " + getParamName(index)).mkString(", ")
  }

  override def doMethodWithBody(context:BaseMethodContext): String = {
    val head = doMethodDefinitionHead(context)
    val body = doMethodBody(context)
    s"$head { $body }"
  }

  def getHeaderFileForClass(clazz:SootClass):String = {
    classNameToPath(clazz.getName) + ".h"
  }

  def doClassHeader(context:BaseClassContext): String = {
    val clazz2 = context.asInstanceOf[CppClassContext]

    val clazz = context.clazz

    val framework = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "framework").asInstanceOf[String]
    val nativeLibrary = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "library").asInstanceOf[String]
    val cflags = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "cflags").asInstanceOf[String]
    val native_header = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "header").asInstanceOf[String]
    
    if (framework != null) clazz2.cppProject.frameworks.add(framework)
    if (nativeLibrary != null) clazz2.cppProject.libraries.add(nativeLibrary)
    if (cflags != null) clazz2.cppProject.cflags.add(nativeLibrary)
    if (native_header != null) clazz2.native_header = native_header

    val classesToInclude = new ListBuffer[SootClass]()
    if (clazz.hasSuperclass) classesToInclude.append(clazz.getSuperclass)

    for (res <- clazz.getInterfaces.asScala) classesToInclude.append(res)


    var declaration = ""
    declaration += "#ifndef " + mangler.mangleFullClassName(clazz.getName) + "_def\n"
    declaration += "#define " + mangler.mangleFullClassName(clazz.getName) + "_def\n"

    declaration += "#include \"types.h\"\n"
    for (res <- classesToInclude) {
      if (res.getName != "java.lang.Object") {
        declaration += "#include \"" + getHeaderFileForClass(res) + "\"\n"
      }
    }
    declaration += "\n"

    if (native_header != null) declaration += native_header + "\n"

    for (rc <- context.referencedClasses) declaration += "class " + mangler.mangle(rc) + ";\n"
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
    for (result <- context.methods) declaration += doMethodDeclaration(result) + "\n"

    if (context.hasStaticConstructor) {
      declaration += "class __StaticInit { public: __StaticInit(); };\n"
      declaration += "private: static __StaticInit* __staticInit;\n"
    }

    declaration += "};\n"

    declaration += "#endif\n"

    declaration
  }

  def doClassBody(context:BaseClassContext): String = {
    val clazz = context.clazz
    val referencedClasses = context.referencedClasses
    var definition = ""

    //for (rc <- referencedClasses) declaration += "#include \"" + Mangling.mangle(rc) + ".h\"\n"

    val classesToInclude = new ListBuffer[SootClass]()

    classesToInclude.append(clazz)
    for (rc <- referencedClasses) classesToInclude.append(rc)

    for (res <- classesToInclude) {
      if (res.getName != "java.lang.Object") {
        definition += "#include \"" + getHeaderFileForClass(res) + "\"\n"
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


    for (result <- context.methods) {
      if (result.methodWithBody != null) {
        definition += result.methodWithBody + "\n"
      } else {
        definition += s"// Not implemented method: {$result.method.getName} (abstract, interface or native)\n"
      }
    }

    if (context.hasStaticConstructor) {
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
    //s"std::shared_ptr< $newType >(new $newType())"
    s"new $newType()"
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
  override def doConstantInt(value: Int, context:BaseMethodContext): String = s"$value"
  override def doConstantLong(value: Long, context:BaseMethodContext): String = s"${value}L"
  override def doConstantFloat(value: Float, context:BaseMethodContext): String = s"${value}f"
  override def doConstantDouble(value: Double, context:BaseMethodContext): String = s"$value"
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
    str.map(c => c match {
      case '"' => "\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => c
    }).mkString("")
  }

  val mangler = CppMangler

  object CppMangler extends BaseMangler {
    def mangle(clazz:SootClass): String = mangleClassName(clazz.getName)
    def mangle(field:SootField): String = field.getName
    def mangleClassName(name:String):String = name.replace('.', '_')
    def mangleFullClassName(name:String):String = name.replace('.', '_')

    //override def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "protected" else "private"
    def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "public" else "public"
    def staticity(member:ClassMember):String = if (member.isStatic) "static" else ""

    def typeToStringRef(kind:Type): String = {
      kind match {
        //case r:RefType => "std::shared_ptr< " + typeToStringNoRef(kind) + " >"
        //case r:ArrayType => "std::shared_ptr< " + typeToStringNoRef(kind) + " >"
        case r:RefType => "" + typeToStringNoRef(kind) + "*"
        case r:ArrayType => "" + typeToStringNoRef(kind) + "*"
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
    def createAPP(path:VfsNode, name:String, executable:Array[Byte], png512x512:Array[Byte]): scala.Unit = {
      val utf8 = Charset.forName("UTF-8")
      path.access("Contents/Frameworks").mkdir()
      path.access("Contents/MacOS/app").ensureParentPath().write(executable)

      Runtime.getRuntime.exec(s"chmod +x " + path.access("Contents/MacOS/app").absoluteFullPath)
      Runtime.getRuntime.exec(s"strip " + path.access("Contents/MacOS/app").absoluteFullPath)

      path.access("Contents/Resources/app.icns").ensureParentPath().write(createIcns(png512x512))
      path.access("Contents/Contents/PkgInfo").ensureParentPath().write("APPL????", utf8)

      path.access("Contents/Contents/Info.plist").write(
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
      , utf8)
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
