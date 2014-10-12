package ast

import org.objectweb.asm.tree._

class AstMethod {
  def process(clazz:ClassNode, method:MethodNode) = {
    new AstBranchAnalyzer(method).analyze(method.instructions)
  }
}
