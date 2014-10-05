package vfs

import java.util.Date
import java.util.zip.ZipFile

import util.FileBytes

class ZipVfsNode(val zip:ZipFile, path:String = "") extends VfsNode {
  lazy val entry = zip.getEntry(path)
  override protected def accessImpl(path: Seq[String]): VfsNode = new ZipVfsNode(zip, this.path + "/" + path.mkString("/"))
  override def write(data: Array[Byte]): Unit = throw new NotImplementedError()
  override def read(): Array[Byte] = FileBytes.read(zip.getInputStream(entry))

  override def stat(): VfsStat = VfsStat(
    entry.getName, entry.getSize, entry.isDirectory,
    new Date(entry.getLastAccessTime.toMillis),
    new Date(entry.getLastModifiedTime.toMillis),
    new Date(entry.getCreationTime.toMillis)
  )

  override def exists(): Boolean = entry != null
  override def size: Long = entry.getSize
}
