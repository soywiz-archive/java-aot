package vfs

import java.nio.ByteBuffer
import java.nio.charset.Charset

import scala.collection.mutable

abstract class VfsNode {
  final def access(path:String):VfsNode = {
    val stack = new mutable.Stack[String]()
    for (item <- path.split('/')) {
      item match {
        case "." | "" =>
        case ".." => stack.pop()
        case _ => stack.push(item)
      }
    }
    accessImpl(stack)
  }
  protected def accessImpl(path:Seq[String]):VfsNode

  def read(): Array[Byte]
  def write(data:Array[Byte]): Unit
  def stat():VfsStat

  def list(): Seq[VfsNode] = List()

  def size:Long = stat().size
  def exists():Boolean = try { size; true } catch { case _:Throwable => false }

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
