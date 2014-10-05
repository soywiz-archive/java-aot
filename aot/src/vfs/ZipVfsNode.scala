package vfs

import java.util.Date
import java.util.zip.ZipFile

import util.FileBytes

class ZipVfsNode(val zip:ZipFile, path:String = "", val name:String = "", val parent:ZipVfsNode = null) extends VfsNode {
  lazy val entry = zip.getEntry(path)
  override protected def child(name: String): VfsNode = new ZipVfsNode(zip, s"${this.path}/$name", name, parent)
  override def write(data: Array[Byte]): Unit = throw new NotImplementedError()
  override def read(): Array[Byte] = FileBytes.read(zip.getInputStream(entry))

  override def list(): Seq[VfsNode] = {
    throw new NotImplementedError()
  }

  def absoluteFullPath:String = zip.getName + "/" + this.fullPath

  override def stat(): VfsStat = VfsStat(
    entry.getName, entry.getSize, entry.isDirectory,
    new Date(entry.getLastAccessTime.toMillis),
    new Date(entry.getLastModifiedTime.toMillis),
    new Date(entry.getCreationTime.toMillis)
  )

  override def exists(): Boolean = entry != null
  override def size: Long = entry.getSize

  override def mkdir(): Unit = throw new NotImplementedError()
  override def remove(): Unit = throw new NotImplementedError()
}
