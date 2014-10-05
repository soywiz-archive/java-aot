package target

import util.RuntimeProvider

import scala.collection.mutable.ListBuffer

class BaseProjectContext(val classNames:Seq[String], val mainClass:String, val runtimeProvider:RuntimeProvider, val outputPath:String) {
  val classes = new ListBuffer[BaseClassContext]

  //def getClassesWithStaticConstructor
}
