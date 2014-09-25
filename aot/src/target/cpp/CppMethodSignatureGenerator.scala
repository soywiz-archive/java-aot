package target.cpp

import soot.SootMethod
import target.base.BaseMethodSignatureGenerator

class CppMethodSignatureGenerator(method:SootMethod) extends BaseMethodSignatureGenerator(method, CppMangler) {

}
