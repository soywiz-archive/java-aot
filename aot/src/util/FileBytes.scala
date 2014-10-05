package util

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.ByteBuffer
import java.nio.charset.Charset

object FileBytes {
  def read(file:File):Array[Byte] = {
    val fis = new FileInputStream(file)
    val data = new Array[Byte](file.length().toInt)
    fis.read(data)
    fis.close()
    data
  }

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
}
