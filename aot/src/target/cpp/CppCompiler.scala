package target.cpp

import target.RuntimeProvider
import target.base.BaseCompiler

object CppCompiler extends BaseCompiler(new RuntimeProvider, CppMangler) {

}
