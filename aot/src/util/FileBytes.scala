package util

import java.io.{InputStream, File, FileInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.charset.Charset

object FileBytes {
  def makeDirectories(file: File) = file.mkdirs()

  def copy(from:File, to:File) = write(to, read(from))
  def read(file:File):Array[Byte] = read(new FileInputStream(file))

  def read(file:File, charset:Charset):String = {
    charset.decode(ByteBuffer.wrap(read(file))).toString
  }

  def write(file:File, data:Array[Byte]):Unit = {
    val fout = new FileOutputStream(file)
    fout.write(data)
    fout.close()
  }

  def write(file:File, charset:Charset, string:String):Unit = {
    val bb = charset.encode(string)
    val data = new Array[Byte](bb.remaining())
    bb.get(data)
    write(file, data)
  }

  def read(is:InputStream): Array[Byte] = {
    val data = new Array[Byte](is.available().toInt)
    is.read(data)
    is.close()
    data
  }
}
