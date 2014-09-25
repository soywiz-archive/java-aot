package target.cpp

import soot.SootMethod
import target.base.BaseMethodBodyGenerator

class CppMethodBodyGenerator(method:SootMethod) extends BaseMethodBodyGenerator(method, CppMangler) {

}
