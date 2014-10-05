package target.as3

import soot.SootMethod
import target.base.{BaseMethodSignatureGenerator, BaseMethodBodyGenerator, BaseMethodGenerator}
import target.cpp.CppMethodBodyGenerator

class As3MethodGenerator(method:SootMethod) extends BaseMethodGenerator(method, As3Mangler) {
  override val bodyGenerator = new CppMethodBodyGenerator(method)
  override val signatureGenerator = new As3MethodSignatureGenerator(method)
}

