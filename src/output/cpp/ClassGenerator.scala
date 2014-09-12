package output.cpp

import soot.{Scene, SootClass}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class ClassGenerator(clazz: SootClass) {
  def this(className: String) = {
    this(Scene.v.loadClassAndSupport(className))
  }

  def doClass(): ClassResult = {
    val results = (for (method <- clazz.getMethods.asScala) yield new MethodGenerator(method).doMethod()).toList

    //println("typedef int int32;")
    //println("typedef long long int int64;")
    //println("class java_lang_Exception;")
    //println("class java_lang_Object;")

    var declaration = ""
    var definition = ""


    if (clazz.hasSuperclass) {
      declaration += "class " + Mangling.mangle(clazz) + " : public " + Mangling.mangle(clazz.getSuperclass) + "{\n"
    } else {
      declaration += "class " + Mangling.mangle(clazz) + " {\n"
    }

    for (field <- clazz.getFields.asScala) {
      declaration += Mangling.visibility(field) + ": " +Mangling.typeToCpp(field.getType) + " " + Mangling.mangle(field) + ";\n"
    }
    for (result <- results) declaration += result.declaration + "\n"
    declaration += "};\n"

    for (result <- results) {
      if (result.definition != null) {
        definition += result.definition + "\n"
      } else {
        definition += "// Native method: " + result.method + "\n"
      }
    }

    val referencedClasses = new ListBuffer[SootClass]
    if (clazz.hasSuperclass) referencedClasses.append(clazz.getSuperclass)
    for (interface <- clazz.getInterfaces.asScala) referencedClasses.append(interface)
    for (result <- results) {
      for (refClass <- result.referencedClasses) {
        referencedClasses.append(refClass)
      }
    }

    ClassResult(clazz, results, declaration, definition, referencedClasses.toList)
  }
}
