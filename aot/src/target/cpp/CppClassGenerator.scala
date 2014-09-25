package target.cpp

import soot.{SootClass, SootMethod}
import target.base.BaseClassGenerator

class CppClassGenerator(clazz: SootClass) extends BaseClassGenerator(clazz, CppMangler) {
  override def createMethodGenerator(method: SootMethod) = new CppMethodGenerator(method)
}
