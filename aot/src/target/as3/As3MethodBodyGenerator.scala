package target.as3

import soot.{IntType, Value, Type, SootMethod}
import target.base.BaseMethodBodyGenerator

abstract class As3MethodBodyGenerator(method:SootMethod) extends BaseMethodBodyGenerator(method, As3Mangler) {
  override def doCast(fromType:Type, toType:Type, value:Value): String = {
    "((" + mangler.typeToStringRef(toType) + ")" + doValue(value) + ")"
  }

  override def doInstanceof(baseType:Type, checkType:Type, value:Value): String = {
    doValue(value) + " is " + mangler.typeToStringRef(checkType)
  }

  override def doBinop(kind:Type, left:Value, right:Value, op:String): String = {
    def castResult(result:String): Unit = {
      kind match {
        case e:IntType =>
      }
    }
    doValue(left) + " " + op + " " + doValue(right)
  }
}
