package target

import soot._
import soot.jimple.{GotoStmt, IfStmt, LookupSwitchStmt, TableSwitchStmt}

import scala.collection.JavaConverters._
import scala.collection.mutable

class BaseMethodContext(val classContext:BaseClassContext, val method: SootMethod) {
  classContext.methods.append(this)

  val locals = new mutable.HashMap[Local, String]
  var lastLocalId = 0

  var methodWithBody:String = ""

  val labels = new mutable.HashMap[Unit, String]
  var lastLabelIndex = 0

  val tryList = new mutable.HashMap[Unit, mutable.ListBuffer[(Int, SootClass)]]
  val catchList = new mutable.HashMap[Unit, mutable.ListBuffer[(Int, SootClass)]]
  val endCatchList = new mutable.HashMap[Unit, mutable.ListBuffer[(Int, SootClass)]]
  def referencedClasses = classContext.referencedClasses

  var usingExceptions = false

  def getReferencedClasses = referencedClasses.toList

  def calculateSignatureDependencies():scala.Unit = {
    for (argType <- method.getParameterTypes.asScala) {
      this.referenceType(argType.asInstanceOf[Type])
    }

    this.referenceType(method.getReturnType)
  }

  var lastTrapId = 0

  def referenceType(kind: Type): scala.Unit = {
    kind match {
      case r: ArrayType => referenceType(r.getElementType)
      case r: RefType => referenceType(r.getSootClass)
      case _ =>
    }
  }

  def referenceType(clazz: SootClass): scala.Unit = {
    referencedClasses.add(clazz)
  }

  def getParamName(index: Int) = s"p$index"

  def allocateLabelName: String = {
    val res = "label_" + lastLabelIndex
    lastLabelIndex += 1
    res
  }

  def allocateLocal(t: Local): String = {
    if (!locals.contains(t)) {
      locals.put(t, "local" + lastLocalId)
      lastLocalId += 1
    }
    locals.get(t).get
  }

  def processLabels(units: List[Unit]): scala.Unit = {
    for (unit <- units) {
      unit match {
        case s: GotoStmt => labels(s.getTarget) = allocateLabelName
        case s: IfStmt => labels(s.getTarget) = allocateLabelName
        case s: LookupSwitchStmt =>
          val targets = (0 to s.getTargetCount - 1).map(s.getTarget)
          for (target <- targets) labels(target) = allocateLabelName
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case s: TableSwitchStmt =>
          for (target <- s.getTargets.asScala) labels(target.asInstanceOf[Unit]) = allocateLabelName
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case _ =>
      }
    }
  }
}

//case class MethodResult(method:SootMethod, declaration:String, definition:String, referencedClasses:List[SootClass])
