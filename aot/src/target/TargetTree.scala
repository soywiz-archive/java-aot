package target

import soot._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class TargetTree {
  private def targetClasses = new mutable.HashMap[SootClass, TargetClass]()

  def getTargetClass(name:String):TargetClass = getTargetClass(Scene.v.getSootClass(name))

  def getTargetClass(clazz:SootClass):TargetClass = {
    //if (!targetClasses.contains(clazz)) targetClasses.put(clazz, new TargetClass(this, clazz))
    //targetClasses.get(clazz).orNull
    new TargetClass(this, clazz)
  }

}

class TargetMethod(val tree:TargetTree, val clazz:TargetClass, val method:SootMethod) {
  lazy val signature = method.getName + "_" + method.getParameterTypes.asScala.map(_.asInstanceOf[Type].toString).mkString(",") + method.getReturnType.toString
}

class TargetField(val tree:TargetTree, val clazz:TargetClass, val field:SootField) {

}

class TargetClass(val tree:TargetTree, val clazz:SootClass) {
  val methods = new mutable.HashMap[String, TargetMethod]
  val fields = new mutable.HashMap[String, TargetField]

  for (sootMethod <- clazz.getMethods.asScala) {
    val targetMethod = new TargetMethod(tree, this, sootMethod)
    methods(targetMethod.signature) = targetMethod
  }


}