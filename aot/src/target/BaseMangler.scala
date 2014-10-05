package target

import soot._

trait BaseMangler {
  def mangle(clazz:SootClass): String
  def mangle(field:SootField): String
  def mangleClassName(name:String):String
  def mangleFullClassName(name:String):String

  def visibility(member:ClassMember):String
  def staticity(member:ClassMember):String

  def typeToStringRef(kind:Type): String

  def isRefType(kind:Type): Boolean = {
    kind match {
      case r:RefType => true
      case r:ArrayType => true
      case _ => false
    }
  }

  def typeToStringNoRef(kind:Type): String

  def mangleBaseName(method: SootMethod): String = {
    val name = method.getName
    name.replace('.', '_').replace('(', '_').replace(')', '_').replace("<", "__").replace(">", "__").replace(" ", "")
  }

  def mangleFullName(method: SootMethod): String = {
    val name = method.getDeclaringClass.getName + "::" + method.getName
    name.replace('.', '_').replace('(', '_').replace(')', '_').replace("<", "__").replace(">", "__").replace(" ", "")
  }
}
