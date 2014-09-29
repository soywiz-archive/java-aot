package target.base

import soot.{SootMethod, Scene, SootClass}
import target.SootUtils
import scala.collection.JavaConverters._

import scala.collection.mutable.{ListBuffer, HashSet}

abstract class BaseClassGenerator(clazz: SootClass, mangler:BaseMangler) {
  def this(className: String, mangler:BaseMangler) = {
    this(Scene.v.loadClassAndSupport(className), mangler)
  }

  def createMethodGenerator(method:SootMethod):BaseMethodGenerator

  def doClass(): ClassResult = {
    val results = (for (method <- clazz.getMethods.asScala) yield createMethodGenerator(method).doMethod()).toList

    val referencedClasses = new HashSet[SootClass]
    if (clazz.hasSuperclass) referencedClasses.add(clazz.getSuperclass)
    for (interface <- clazz.getInterfaces.asScala) referencedClasses.add(interface)
    for (result <- results) {
      for (refClass <- result.referencedClasses) {
        referencedClasses.add(refClass)
      }
    }
    referencedClasses.remove(this.clazz)

    var staticConstructor:StaticConstructorResult = null

    try {
      val clinitMethod = clazz.getMethodByName("<clinit>")
      val className = mangler.mangle(clazz)
      staticConstructor = StaticConstructorResult(clazz)
    } catch  {
      case e:Exception =>
    }

    val native_framework = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "framework").asInstanceOf[String]
    val native_library = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "library").asInstanceOf[String]
    val cflags = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "cflags").asInstanceOf[String]
    val native_header = SootUtils.getTag(clazz.getTags.asScala, "Llibcore/CPPClass;", "header").asInstanceOf[String]

    //println("typedef int int32;")
    //println("typedef long long int int64;")
    //println("class java_lang_Exception;")
    //println("class java_lang_Object;")

    var declaration = ""
    var definition = ""

    declaration += "#ifndef " + mangler.mangleFullClassName(clazz.getName) + "_def\n"
    declaration += "#define " + mangler.mangleFullClassName(clazz.getName) + "_def\n"

    declaration += "#include \"types.h\"\n"
    if (native_header != null) {
      declaration += native_header + "\n"
    }
    //for (rc <- referencedClasses) declaration += "#include \"" + Mangling.mangle(rc) + ".h\"\n"
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
          definition += s"$mangledFieldType $mangledClassType::$mangledFieldName = ($mangledFieldType)(void*)0;\n"
        } else {
          definition += s"$mangledFieldType $mangledClassType::$mangledFieldName = ($mangledFieldType)0;\n"
        }
      }
    }

    for (result <- results) {
      if (result.definition != null) {
        definition += result.definition + "\n"
      } else {
        definition += "// Native method: " + result.method + "\n"
      }
    }

    if (staticConstructor != null) {
      definition += s"$mangledClassType::__StaticInit::__StaticInit() { $mangledClassType::__clinit__(); }\n"
      definition += s"$mangledClassType::__StaticInit* $mangledClassType::__staticInit = new $mangledClassType::__StaticInit();\n"

    }

    ClassResult(clazz, results, declaration, definition, referencedClasses.toList, native_framework, native_library, cflags, staticConstructor)
  }
}
