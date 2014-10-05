package vfs

import java.io.File
import java.util.Date

import util.FileBytes

class FileVfsNode(val path:String) extends VfsNode {
  lazy val file = new File(path)
  override protected def accessImpl(path: Seq[String]): VfsNode = new FileVfsNode(this.path + "/" + path.mkString("/"))
  override def write(data: Array[Byte]): Unit = FileBytes.write(file, data)
  override def read(): Array[Byte] = FileBytes.read(file)

  override def stat(): VfsStat = {
    VfsStat(file.getName, file.length(), file.isDirectory, new Date(file.lastModified()), new Date(file.lastModified()), new Date(file.lastModified()))
  }

  override def exists(): Boolean = file.exists()
  override def size: Long = file.length()
}
