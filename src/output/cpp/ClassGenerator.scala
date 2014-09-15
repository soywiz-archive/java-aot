package output.cpp

import output.cpp.Mangling
import soot.{Scene, SootClass}

import scala.collection.JavaConverters._
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

class ClassGenerator(clazz: SootClass) {
  def this(className: String) = {
    this(Scene.v.loadClassAndSupport(className))
  }

  def doClass(): ClassResult = {
    val results = (for (method <- clazz.getMethods.asScala) yield new MethodGenerator(method).doMethod()).toList

    val referencedClasses = new HashSet[SootClass]
    if (clazz.hasSuperclass) referencedClasses.add(clazz.getSuperclass)
    for (interface <- clazz.getInterfaces.asScala) referencedClasses.add(interface)
    for (result <- results) {
      for (refClass <- result.referencedClasses) {
        referencedClasses.add(refClass)
      }
    }
    referencedClasses.remove(this.clazz)

    //println("typedef int int32;")
    //println("typedef long long int int64;")
    //println("class java_lang_Exception;")
    //println("class java_lang_Object;")

    var declaration = ""
    var definition = ""

    declaration += "#ifndef " + Mangling.mangleFullClassName(clazz.getName) + "_def\n"
    declaration += "#define " + Mangling.mangleFullClassName(clazz.getName) + "_def\n"

    declaration += "#include \"types.h\"\n"
    for (rc <- referencedClasses) declaration += "#include \"" + Mangling.mangle(rc) + ".h\"\n"

    declaration += "\n"

    declaration += "class " + Mangling.mangle(clazz)
    if (clazz.hasSuperclass) declaration += " : public " + Mangling.mangle(clazz.getSuperclass)
    declaration += " {\n"

    for (field <- clazz.getFields.asScala) {
      declaration += Mangling.visibility(field) + ": " + Mangling.staticity(field) + " " + Mangling.typeToCppRef(field.getType) + " " + Mangling.mangle(field) + ";\n"
    }
    for (result <- results) declaration += result.declaration + "\n"
    declaration += "};\n"

    declaration += "#endif\n"

    definition += "#include \"" + Mangling.mangleFullClassName(clazz.getName) + ".h\"\n"

    for (field <- clazz.getFields.asScala) {
      if (field.isStatic) {
        definition += Mangling.typeToCppRef(field.getType) + " " + Mangling.mangleClassName(clazz.getName) + "::" + Mangling.mangle(field) + " = (" + Mangling.typeToCppRef(field.getType) + ")(void *)0;\n"
      }
    }

    for (result <- results) {
      if (result.definition != null) {
        definition += result.definition + "\n"
      } else {
        definition += "// Native method: " + result.method + "\n"
      }
    }

    ClassResult(clazz, results, declaration, definition, referencedClasses.toList)
  }
}
