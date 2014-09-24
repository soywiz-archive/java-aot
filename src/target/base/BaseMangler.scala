package target.base

import soot._

trait BaseMangler {
  def mangle(clazz:SootClass): String
  def mangle(field:SootField): String
  def mangleClassName(name:String):String
  def mangleFullClassName(name:String):String

  def visibility(member:ClassMember):String
  def staticity(member:ClassMember):String

  def typeToCppRef(kind:Type): String

  def typeToCppNoRef(kind:Type): String
}
