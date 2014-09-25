package target.as3

import soot._
import target.base.BaseMangler

object As3Mangler extends BaseMangler {
  override def mangle(clazz:SootClass): String = mangleClassName(clazz.getName)
  override def mangle(field:SootField): String = field.getName
  override def mangleClassName(name:String):String = name.replace('.', '_')
  override def mangleFullClassName(name:String):String = name.replace('.', '_')

  override def visibility(member:ClassMember):String = if (member.isPublic) "public" else if (member.isProtected) "protected" else "private"
  override def staticity(member:ClassMember):String = if (member.isStatic) "static" else ""

  override def typeToCppRef(kind:Type): String = {
    typeToCppNoRef(kind)
  }

  override def typeToCppNoRef(kind:Type): String = {
    kind match {
      case v:VoidType => "void"
      case v:NullType => "null"
      case prim:PrimType =>
        prim match {
          case v:BooleanType => "Boolean"
          case v:ByteType => "int"
          case v:CharType => "int"
          case v:ShortType => "int"
          case v:IntType => "int"
          case v:LongType => "Long"
          case v:FloatType => "Number"
          case v:DoubleType => "Number"
        }
      case r:ArrayType => "Array<" + typeToCppRef(r.getElementType) + ">"
      case r:RefType => mangleClassName(r.getClassName)
    }
  }
}
