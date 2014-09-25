package target.as3

import soot.SootMethod
import target.base.{BaseMethodSignatureGenerator, BaseMethodBodyGenerator, BaseMethodGenerator}
import target.cpp.CppMangler

class As3MethodGenerator(method:SootMethod) extends BaseMethodGenerator(method, As3Mangler) {
  override val bodyGenerator = new As3MethodBodyGenerator(method)
  override val signatureGenerator = new As3MethodSignatureGenerator(method)
}

