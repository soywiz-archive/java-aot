package target.cpp

import soot._
import target.base.{BaseMethodBodyGenerator, BaseMethodSignatureGenerator, BaseMethodGenerator}

class CppMethodGenerator(method: SootMethod) extends BaseMethodGenerator(method, CppMangler) {
  override val bodyGenerator = new CppMethodBodyGenerator(method)
  override val signatureGenerator = new CppMethodSignatureGenerator(method)
}

