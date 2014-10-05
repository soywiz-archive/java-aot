package vfs

import java.util.Date

case class VfsStat(name:String, size:Long, isDirectory:Boolean, lastAccess:Date, lastModified:Date, created:Date) {
  def isFile = !isDirectory
}
