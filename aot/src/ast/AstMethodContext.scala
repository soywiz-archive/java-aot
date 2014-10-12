package ast

import org.objectweb.asm.tree.LabelNode

import scala.collection.mutable

class AstMethodContext {
  private var lastLabel:Int = 0
  private var lastLocal:Int = 0
  private val map = new mutable.HashMap[LabelNode, LabelRef]()

  def getLabelRef(node: LabelNode): LabelRef = {
    if (!map.contains(node)) map(node) = allocLabelRef()
    map(node)
  }

  def allocLabelRef() = {
    lastLabel += 1
    LabelRef(lastLabel - 1)
  }

  def allocLocal(kind:NodeType, prefix:String = "temp_"): Local = {
    lastLocal += 1
    Local(kind, lastLocal - 1, prefix + (lastLocal - 1))
  }
}
