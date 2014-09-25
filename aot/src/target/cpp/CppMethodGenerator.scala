package target.cpp

import soot._
import target.base.BaseMethodGenerator

class CppMethodGenerator(method: SootMethod) extends BaseMethodGenerator(method, new CppMangler) {
}

