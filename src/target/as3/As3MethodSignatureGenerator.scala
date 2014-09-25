package target.as3

import soot.SootMethod
import target.base.{BaseMethodSignatureGenerator, BaseMethodBodyGenerator}

class As3MethodSignatureGenerator(method:SootMethod) extends BaseMethodSignatureGenerator(method, As3Mangler) {

}
