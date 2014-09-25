package target.as3

import soot.{Value, Type, SootMethod}
import target.base.BaseMethodBodyGenerator

class As3MethodBodyGenerator(method:SootMethod) extends BaseMethodBodyGenerator(method, As3Mangler) {
  override def doCast(fromType:Type, toType:Type, value:Value): String = {
    "((" + mangler.typeToCppRef(toType) + ")" + doValue(value) + ")"
  }

  override def doInstanceof(baseType:Type, checkType:Type, value:Value): String = {
    doValue(value) + " instanceof " + mangler.typeToCppRef(checkType)
  }

  override def doBinop(kind:Type, left:Value, right:Value, op:String): String = {
    doValue(left) + " " + op + " " + doValue(right)
  }
}
