package target.cpp

import soot.{SootClass, SootMethod}
import target.base.BaseClassGenerator

class CppClassGenerator(clazz: SootClass) extends BaseClassGenerator(clazz, new CppMangler()) {
  override def createMethodGenerator(method: SootMethod) = new CppMethodGenerator(method)
}
