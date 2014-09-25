package target.base

import soot.SootMethod

class BaseMethodSignatureGenerator(method: SootMethod, mangler:BaseMangler) {
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
      //declaration += "virtual "
      if (method.isAbstract) {
        declaration += "virtual "
      }
    }
    declaration += s"$returnType $mangledBaseName($params)"
    if (method.isAbstract) {
      declaration += " = 0"
    }

    declaration += ";"

    declaration
  }

  def generateBody(body:String) = {
    s"$returnType $mangledFullName($params) { $body }"
  }

  private def getParamName(index: Int) = s"p$index"
}
