package target

import util.RuntimeProvider
import vfs.VfsNode

import scala.collection.mutable.ListBuffer

class BaseProjectContext(val classNames:Seq[String], val mainClass:String, val runtime:RuntimeProvider, val output:VfsNode) {
  val classes = new ListBuffer[BaseClassContext]
  val preInitLines = new ListBuffer[String]
  val bootImports = new ListBuffer[String]

  //def getClassesWithStaticConstructor
}
