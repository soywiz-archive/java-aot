package target

import soot.SootClass

import scala.collection.mutable.ListBuffer

class BaseClassContext(val projectContext:BaseProjectContext, val clazz:SootClass) {
  val referencedClasses = new scala.collection.mutable.HashSet[SootClass]
  val methods = new ListBuffer[BaseMethodContext]

  projectContext.classes.append(this)

  //case class ClassResult(clazz: SootClass, methods: List[MethodResult], declaration: String, definition: String, referencedClasses: List[SootClass], nativeFramework: String, nativeLibrary: String, cflags: String, staticConstructor:StaticConstructorResult)

}
