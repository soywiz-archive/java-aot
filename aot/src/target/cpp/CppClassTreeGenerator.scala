package target.cpp

import soot.SootClass
import target.RuntimeProvider
import target.base.{BaseClassGenerator, BaseClassTreeGenerator}

class CppClassTreeGenerator(runtimeProvider:RuntimeProvider) extends BaseClassTreeGenerator(runtimeProvider, CppMangler, CppCompiler, CppRunner) {
  override def createClassGenerator(item: SootClass): BaseClassGenerator = new CppClassGenerator(item)
}
