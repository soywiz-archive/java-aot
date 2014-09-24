package target.as3

import soot.SootMethod
import target.base.BaseMethodGenerator
import target.cpp.CppMangler

class As3MethodGenerator(method:SootMethod) extends BaseMethodGenerator(method, new CppMangler) {
}

