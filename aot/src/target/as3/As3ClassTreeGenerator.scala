package target.as3

import soot.SootClass
import target.RuntimeProvider
import target.base.{BaseClassGenerator, BaseClassTreeGenerator}

class As3ClassTreeGenerator(runtimeProvider:RuntimeProvider) extends BaseClassTreeGenerator(runtimeProvider, As3Mangler, As3Compiler, As3Runner) {
  override def createClassGenerator(item: SootClass): BaseClassGenerator = new As3ClassGenerator(item)
}
