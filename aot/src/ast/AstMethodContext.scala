package ast

import org.objectweb.asm.tree._
import scala.collection.mutable
import scala.collection.JavaConverters._

class AstMethodContext(method:MethodNode = null) {
  private var lastLabel:Int = 0
  private var lastLocal:Int = 0
  private val map = new mutable.HashMap[LabelNode, LabelRef]()
  def debug = false

  def getLabelRef(node: LabelNode): LabelRef = {
    if (!map.contains(node)) map(node) = allocLabelRef()
    map(node)
  }

  def allocLabelRef() = {
    lastLabel += 1
    LabelRef(lastLabel - 1)
  }

  def getLocalIn(node:AbstractInsnNode, index:Int): Local = {
    val nodeIndex = method.instructions.indexOf(node)
    for (local <- method.localVariables.asScala.map(_.asInstanceOf[LocalVariableNode])) {
      val startIndex = method.instructions.indexOf(local.start)
      val endIndex = method.instructions.indexOf(local.end)

      if ((startIndex until endIndex).contains(nodeIndex)) {
        return Local(NodeUtils.typeFromDesc(local.desc), -1, local.name)
      }
    }
    throw new Exception("Can't find local")
  }

  def allocLocal(kind:NodeType, prefix:String = "temp_"): Local = {
    lastLocal += 1
    Local(kind, lastLocal - 1, prefix + (lastLocal - 1))
  }
}
