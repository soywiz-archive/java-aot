package ast

import org.objectweb.asm.tree.{InsnList, AbstractInsnNode}

object InsUtils {
  def createList(nodes:Seq[AbstractInsnNode]) = {
    val list = new InsnList()
    for (node <- nodes) {
      list.add(node)
    }
    list
  }
}
