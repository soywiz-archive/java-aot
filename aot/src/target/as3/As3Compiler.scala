package target.as3

import target.RuntimeProvider
import target.base.BaseCompiler

object As3Compiler extends BaseCompiler(new RuntimeProvider, As3Mangler) {

}
