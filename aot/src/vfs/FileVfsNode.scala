package vfs

import java.io.File
import java.util.Date

import util.FileBytes
import scala.collection.JavaConverters._

class FileVfsNode(val path:String, val name:String = "", val parent:VfsNode = null) extends VfsNode {
  lazy val file = new File(path)
  override protected def child(name: String): VfsNode = new FileVfsNode(s"${this.path}/$name", name, this)
  override def write(data: Array[Byte]): Unit = FileBytes.write(file, data)
  override def read(): Array[Byte] = FileBytes.read(file)

  override def list(): Seq[VfsNode] = file.list().map(name => child(name))

  def absoluteFullPath:String = path

  override def stat(): VfsStat = {
    VfsStat(file.getName, file.length(), file.isDirectory, new Date(file.lastModified()), new Date(file.lastModified()), new Date(file.lastModified()))
  }

  override def exists(): Boolean = file.exists()
  override def size: Long = file.length()
  override def mkdir(): Unit = file.mkdirs()
  override def remove(): Unit = file.delete()
}
