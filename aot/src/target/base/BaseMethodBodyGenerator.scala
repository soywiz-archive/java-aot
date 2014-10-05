package target.base

import _root_.soot.jimple.TableSwitchStmt
import soot._
import soot.jimple._

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

abstract class BaseMethodBodyGenerator(method: SootMethod, protected val mangler: BaseMangler) {
  private val locals = new mutable.HashMap[Local, String]
  private var lastLocalId = 0

  protected val labels = new mutable.HashMap[Unit, String]
  private var lastLabelIndex = 0

  private val tryList = new mutable.HashMap[Unit, mutable.ListBuffer[Tuple2[Int, SootClass]]]
  private val catchList = new mutable.HashMap[Unit, mutable.ListBuffer[Tuple2[Int, SootClass]]]
  private val endCatchList = new mutable.HashMap[Unit, mutable.ListBuffer[Tuple2[Int, SootClass]]]
  private val referencedClasses = new mutable.HashSet[SootClass]

  def getReferencedClasses = referencedClasses.toList

  def calculateSignatureDependencies():scala.Unit = {
    for (argType <- method.getParameterTypes.asScala) {
      this.referenceType(argType.asInstanceOf[Type])
    }

    this.referenceType(method.getReturnType)
  }

  var lastTrapId = 0

  def generateBody(): String = {

    val body = method.retrieveActiveBody
    for (trap <- body.getTraps.asScala) {
      val trapId = lastTrapId
      lastTrapId += 1
      println(s"handler: ${trap.getBeginUnit}, ${trap.getHandlerUnit}, ${trap.getEndUnit}")
      if (!tryList.contains(trap.getBeginUnit)) tryList(trap.getBeginUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      if (!catchList.contains(trap.getHandlerUnit)) catchList(trap.getHandlerUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      if (!endCatchList.contains(trap.getEndUnit)) endCatchList(trap.getEndUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      tryList(trap.getBeginUnit).append((trapId, trap.getException))
      catchList(trap.getHandlerUnit).append((trapId, trap.getException))
      endCatchList(trap.getEndUnit).append((trapId, trap.getException))
      //println(trap)
    }
    val units = body.getUnits

    getLabels(units.asScala.toList)

    var stms = ""
    for (unit <- units.asScala) {
      stms += this.doUnit(unit) + "\n"
    }

    var bodyString = "\n"
    if (usingExceptions) {
      bodyString += doVariableAllocation(RefType.v("java.lang.Throwable"), "__caughtexception")
    }
    for (local2 <- locals) {
      val (local, name) = local2
      bodyString += doVariableAllocation(local.getType, name)
    }
    bodyString += stms
    bodyString
  }

  def getLabels(units: List[Unit]): scala.Unit = {
    for (unit <- units) {
      unit match {
        case s: GotoStmt => labels(s.getTarget) = allocateLabelName
        case s: IfStmt => labels(s.getTarget) = allocateLabelName
        case s: LookupSwitchStmt =>
          val targets = (0 to s.getTargetCount - 1).map(s.getTarget(_).asInstanceOf[Unit])
          for (target <- targets) labels(target) = allocateLabelName
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case s: TableSwitchStmt =>
          for (target <- s.getTargets.asScala) labels(target.asInstanceOf[Unit]) = allocateLabelName
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case _ =>
      }
    }
  }

  var usingExceptions = false

  final def doUnit(unit: soot.Unit): String = {
    def processLabel = if (labels.contains(unit)) doLabel(labels(unit)) else ""
    def processTry = if (tryList.contains(unit)) tryList(unit).map(_ => doTryStart()).mkString else ""
    def processEndCatch = {
      if (endCatchList.contains(unit)) {
        endCatchList(unit).map(item => {
          val (trapId, trapType) = item
          doCatchAndGoto(trapType, s"exception_handler_$trapId")
        }).mkString
      } else {
        ""
      }
    }
    def processExceptionHandler = {
      if (catchList.contains(unit)) {
        catchList(unit).map(item => {
          val (trapId) = item
          usingExceptions = true
          doLabel(s"exception_handler_$trapId") + "\n"
        }).mkString
      } else {
        ""
      }
    }
    def processUnit = _doUnit(unit)

    List(processLabel, processTry, processEndCatch, processExceptionHandler, processUnit).mkString
  }

  final def _doUnit(unit: soot.Unit): String = {
    unit match {
      case s: DefinitionStmt => referenceType(s.getLeftOp.getType); doAssign(s.getLeftOp, s.getRightOp)
      case s: ReturnStmt => doReturn(method.getReturnType, s.getOp)
      case s: ReturnVoidStmt => doReturnVoid()
      case s: IfStmt => doIf(s.getCondition, s.getTarget)
      case s: GotoStmt => doGoto(s.getTarget)
      case s: ThrowStmt => doThrow(s.getOp)
      case s: InvokeStmt => doExprStm(s.getInvokeExpr)
      case s: EnterMonitorStmt => doEnterMonitor(s.getOp)
      case s: ExitMonitorStmt => doExitMonitor(s.getOp)
      case s: NopStmt => doNop()
      case s: LookupSwitchStmt =>
        doSwitch(s.getKey, labels(s.getDefaultTarget),
          uniqueMap((0 until s.getTargetCount).map(i => (s.getLookupValue(i), labels(s.getTarget(i))))).orNull
        )
      case s: TableSwitchStmt =>
        doSwitch(s.getKey, labels(s.getDefaultTarget),
          uniqueMap((s.getLowIndex to s.getHighIndex).map(i => (i, labels(s.getTarget(i - s.getLowIndex))))).orNull
        )
    }
    //unit.addBoxPointingToThis()
    //println("  unit:" + unit)
  }

  private def uniqueMap[A,B](s: Seq[(A,B)]) = {
    val h = new collection.mutable.HashMap[A,B]
    val okay = s.iterator.forall(x => {
      val y = h.put(x._1, x._2)
      y.isEmpty || y.get == x._2
    })
    if (okay) Some(h) else None
  }

  final def doValue(value: Value): String = {
    value match {
      case t: Local => doLocal(allocateLocal(t))
      case t: Immediate => doImmediate(t)
      case t: ThisRef => doThisRef(method.getDeclaringClass)
      case t: ParameterRef => doParam(getParamName(t.getIndex))
      case t: CaughtExceptionRef => referenceType(t.getType); doCaughtException(t.getType)
      case t: ArrayRef => doArrayAccess(t.getBase, t.getIndex)
      case t: InstanceFieldRef => referenceType(t.getField.getDeclaringClass); doInstanceField(t.getBase, t.getField.getName)
      case t: StaticFieldRef => referenceType(t.getField.getDeclaringClass); doStaticField(t.getField.getDeclaringClass, t.getField.getName)
      case t: Expr => doExpr(t)
    }
  }

  final def doImmediate(i: Immediate): String = {
    i match {
      case c: NullConstant => doConstantNull()
      case c: IntConstant => doConstantInt(c.value)
      case c: LongConstant => doConstantLong(c.value)
      case c: FloatConstant => doConstantFloat(c.value)
      case c: DoubleConstant => doConstantDouble(c.value)
      case c: StringConstant => doConstantString(c.value)
      case c: ClassConstant => doConstantClass(c.value)
    }
  }

  final def doExpr(expr: Expr): String = {
    expr match {
      case e: CastExpr => referenceType(e.getCastType); doCast(e.getType, e.getCastType, e.getOp)
      case e: InstanceOfExpr => referenceType(e.getType); doInstanceof(e.getType, e.getCheckType, e.getOp)
      case e: NewExpr => referenceType(e.getType); doNew(e.getType)
      case e: NewArrayExpr => referenceType(e.getType.getArrayType); doNewArray(e.getType, e.getSize)
      case e: NewMultiArrayExpr => referenceType(e.getType); doNewMultiArray(e.getType, (0 to e.getSizeCount - 1).map(e.getSize).toArray)
      case e: LengthExpr => doLength(e.getOp)
      case e: NegExpr => doNegate(e.getOp)
      case e: BinopExpr => doBinop(e.getType, e.getOp1, e.getOp2, getOpString(e))
      case e: InvokeExpr =>
        referenceType(e.getMethod.getDeclaringClass)
        val argsList = e.getArgs.asScala.toList
        val castTypes = e.getMethod.getParameterTypes.asScala.map(_.asInstanceOf[Type]).toList
        val args = (argsList, castTypes).zipped.map((value, expectedType) => doCastIfNeeded(expectedType, value)).toList
        e.getArgs.asScala.foreach(i => referenceType(i.getType))
        e match {
          case i: StaticInvokeExpr => doInvokeStatic(e.getMethod, args)
          case i: InstanceInvokeExpr => doInvokeInstance(i.getBase, e.getMethod, args, i.isInstanceOf[SpecialInvokeExpr])
        }
    }
  }

  final private def getOpString(e: BinopExpr): String = {
    e match {
      case k: AddExpr => "+" case k: SubExpr => "-"
      case k: MulExpr => "*" case k: DivExpr => "/" case k: RemExpr => "%"
      case k: AndExpr => "&" case k: OrExpr => "|" case k: XorExpr => "^"
      case k: ShlExpr => "<<" case k: ShrExpr => ">>" case k: UshrExpr => ">>>"
      case k: EqExpr => "==" case k: NeExpr => "!="
      case k: GeExpr => ">=" case k: LeExpr => "<="
      case k: LtExpr => "<" case k: GtExpr => ">"
      case k: CmpExpr => "cmp" case k: CmplExpr => "cmpl" case k: CmpgExpr => "cmpg"
    }
  }

  final def doCastIfNeeded(toType:Type, value:Value): String = {
    if (value.getType.equals(toType)) {
      doValue(value)
    } else {
      doCast(value.getType, toType, value)
    }
  }

  def doStringLiteral(s: String): String
  def doCast(fromType:Type, toType:Type, value:Value): String
  def doInstanceof(baseType:Type, checkType:Type, value:Value): String
  def doBinop(kind:Type, left:Value, right:Value, op:String): String
  def doNegate(value:Value):String
  def doLength(value:Value):String
  def doStaticField(kind:SootClass, fieldName:String): String
  def doNew(kind:Type): String
  def doVariableAllocation(kind:Type, name:String): String
  def doNewArray(kind: Type, size: Value): String
  def doNewMultiArray(kind: Type, values: Array[Value]): String
  def doNop(): String
  def doCaughtException(value: Type): String
  def doArrayAccess(value: Value, value1: Value): String
  def doGoto(unit: Unit): String
  def doReturn(returnType: Type, returnValue: Value): String
  def doReturnVoid(): String
  def doIf(condition: Value, target: Stmt): String
  def doConstantClass(s: String): String
  def doConstantNull(): String
  def doConstantInt(value: Int): String
  def doConstantLong(value: Long): String
  def doConstantFloat(value: Float): String
  def doConstantDouble(value: Double): String
  def doConstantString(value: String): String
  def doThrow(value: Value): String
  def doLabel(labelName: String): String
  def doTryStart():String
  def doCatchAndGoto(trapType: SootClass, labelName: String): String
  def doSwitch(matchValue:Value, defaultLabel: String, map: mutable.HashMap[Int, String]): String
  def doAssign(leftOp: Value, rightOp: Value): String
  def doExprStm(expr: Expr): String
  def doThisRef(clazz: SootClass): String
  def doEnterMonitor(value: Value): String
  def doExitMonitor(value: Value): String
  def doLocal(localName: String): String
  def doParam(paramName: String): String
  def doInstanceField(instance: Value, fieldName: String): String
  def doInvokeStatic(method: SootMethod, args: Seq[String]): String
  def doInvokeInstance(base: Value, method: SootMethod, args: List[String], special:Boolean): String

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

}
