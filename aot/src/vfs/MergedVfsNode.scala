package vfs

import com.sun.javaws.exceptions.InvalidArgumentException

class MergedVfsNode(nodes:Seq[VfsNode], val name:String = "", val parent:VfsNode = null) extends VfsNode {
  if (nodes.isEmpty) throw new InvalidArgumentException(Array("Nodes can't be empty"))

  override protected def child(name:String): VfsNode = new MergedVfsNode(nodes.map(node => node.access(name)), name, this)
  
  override def absoluteFullPath: String = "merged@" + this.fullPath

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
  override def mkdir(): Unit = op(_.mkdir())
  override def remove(): Unit = op(_.remove())
}
