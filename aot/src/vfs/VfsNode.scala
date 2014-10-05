package vfs

import java.nio.ByteBuffer
import java.nio.charset.Charset

abstract class VfsNode {
  final def access(path:String):VfsNode = access(path.split('/'))

  final private def access(chunks:Seq[String]):VfsNode = {
    if (chunks.isEmpty) {
      this
    } else {
      (chunks.head match {
        case null | "." | "" => this
        case ".." => this.parent
        case _ => this.child(chunks.head)
      }).access(chunks.tail)
    }
  }

  protected def child(element:String):VfsNode

  def absoluteFullPath:String
  final def fullPath:String = if (parent != null) s"${parent.fullPath}/$name" else name
  def name:String
  def parent:VfsNode
  def read(): Array[Byte]
  def write(data:Array[Byte]): Unit
  def stat():VfsStat
  def mkdir():Unit
  def remove():Unit

  final def ensureParentPath():VfsNode = {
    if (parent != null) parent.mkdir()
    this
  }

  def list(): Seq[VfsNode] = List()

  def isDirectory:Boolean = stat().isDirectory
  def size:Long = stat().size
  def exists():Boolean = try { size; true } catch { case _:Throwable => false }

  def copyTo(that:VfsNode): Unit = that.ensureParentPath().write(this.read())

  def copyTreeTo(that:VfsNode):Unit = {
    println("copyTreeTo " + this.absoluteFullPath + " -> " + that.absoluteFullPath)
    val stat = this.stat()
    if (stat.isDirectory) {
      that.mkdir()
      for (node <- this.list()) {
        node.copyTreeTo(that.access(node.name))
      }
    } else {
      this.copyTo(that)
    }
  }

  final def read(charset:Charset):String = {
    charset.decode(ByteBuffer.wrap(read())).toString
  }

  final def write(string:String, charset:Charset):Unit = {
    val bb = charset.encode(string)
    val data = new Array[Byte](bb.remaining())
    bb.get(data)
    write(data)
  }
}
