package target.as3

import soot.{SootClass, SootMethod}
import target.base.BaseClassGenerator

class As3ClassGenerator(clazz: SootClass) extends BaseClassGenerator(clazz, As3Mangler) {
  override def createMethodGenerator(method: SootMethod) = new As3MethodGenerator(method)
}
