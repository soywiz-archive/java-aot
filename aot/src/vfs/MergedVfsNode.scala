package vfs

import com.sun.javaws.exceptions.InvalidArgumentException

class MergedVfsNode(nodes:Seq[VfsNode]) extends VfsNode {
  if (nodes.isEmpty) throw new InvalidArgumentException(Array("Nodes can't be empty"))

  override protected def accessImpl(path: Seq[String]): VfsNode = new MergedVfsNode(nodes.map(node => node.accessImpl(path)))

  private def op[T](action: VfsNode => T): T = {
    var lastError:Throwable = null
    for (node <- nodes) {
      try {
        return action(node)
      } catch {
        case t:Throwable => lastError = t
      }
    }
    throw lastError
  }

  override def write(data: Array[Byte]): Unit = op(_.write(data))
  override def read(): Array[Byte] = op(_.read())
  override def stat(): VfsStat = op(_.stat())
}
