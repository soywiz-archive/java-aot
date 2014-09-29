package target.base

import _root_.soot.jimple.TableSwitchStmt
import soot._
import soot.jimple._

import scala.collection.mutable
import scala.collection.JavaConverters._

class BaseMethodBodyGenerator(method: SootMethod, protected val mangler: BaseMangler) {
  private var locals = new mutable.HashMap[Local, String]
  private var lastLocalId = 0

  private val labels = new mutable.HashMap[Unit, String]
  private var lastLabelIndex = 0

  private val tryList = new mutable.HashMap[Unit, Tuple2[Int, SootClass]]
  private val catchList = new mutable.HashMap[Unit, Tuple2[Int, SootClass]]
  private val endCatchList = new mutable.HashMap[Unit, Tuple2[Int, SootClass]]
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
      tryList(trap.getBeginUnit) = (trapId, trap.getException)
      catchList(trap.getHandlerUnit) = (trapId, trap.getException)
      endCatchList(trap.getEndUnit) = (trapId, trap.getException)
      //println(trap)
    }
    val units = body.getUnits

    getLabels(units.asScala.toList)

    var stms = ""
    for (unit <- units.asScala) {
      stms += this.doUnit(unit) + "\n"
    }

    var bodyString = "\n"
    if (usingExceptions) bodyString += "java_lang_Throwable* __caughtexception;"
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
        //case s: IfStmt => getLabels(List(s.getTarget))
        case s: IfStmt => labels(s.getTarget) = allocateLabelName
        case s: LookupSwitchStmt =>
          for (i <- 0 to s.getTargetCount - 1) {
            labels(s.getTarget(i).asInstanceOf[Unit]) = allocateLabelName
          }
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case s: TableSwitchStmt =>
          for (target <- s.getTargets.asScala) labels(target.asInstanceOf[Unit]) = allocateLabelName
          if (s.getDefaultTarget != null) labels(s.getDefaultTarget) = allocateLabelName
        case _ =>
      }
    }
  }

  var usingExceptions = false

  def doUnit(unit: soot.Unit): String = {
    var out = ""

    if (labels.contains(unit)) {
      out += labels(unit) + ":; "
    }

    if (tryList.contains(unit)) {
      out += "try {\n"
    }

    if (endCatchList.contains(unit)) {
      val (trapId, trapType) = endCatchList(unit)
      out += "} catch (" + mangler.mangle(trapType) + s"* __caughtexception__) { __caughtexception = __caughtexception__; goto exception_handler_$trapId; }\n"
    }

    if (catchList.contains(unit)) {
      val (trapId, trapType) = catchList(unit)
      usingExceptions = true
      out += s"exception_handler_$trapId:;\n"
    }

    out += _doUnit(unit)
    out
  }

  def _doUnit(unit: soot.Unit): String = {
    var stms = ""
    unit match {
      case s: DefinitionStmt =>
        referenceType(s.getLeftOp.getType)
        if (s.getRightOp.getType.equals(s.getLeftOp.getType)) {
          doValue(s.getLeftOp) + " = " + doValue(s.getRightOp) + ";"
        } else {
          // Exceptions for example!
          doValue(s.getLeftOp) + " = ((" + mangler.typeToStringRef(s.getLeftOp.getType) + ")" + doValue(s.getRightOp) + ");"
        }
      case s: ReturnStmt => "return " + doValue(s.getOp) + ";"
      case s: ReturnVoidStmt => "return;"
      case s: IfStmt => s"if (" + doValue(s.getCondition) + ") { goto " + labels(s.getTarget) + "; }"
      case s: LookupSwitchStmt =>
        var out = ""
        var index = 0
        for (i <- 0 to s.getTargetCount - 1) {
          out += "case " + s.getLookupValue(i) + ": goto " + labels(s.getTarget(i)) + "; break;\n"
        }
        out += "default: goto " + labels(s.getDefaultTarget) + "; break;\n"
        "switch(" + doValue(s.getKey) + ") {\n" + out + "}"
      case s: TableSwitchStmt =>
        var out = ""
        var index = 0
        for (i <- s.getLowIndex to s.getHighIndex) {
          out += "case " + i + ": goto " + labels(s.getTarget(index)) + "; break;\n"
          index += 1
        }
        out += "default: goto " + labels(s.getDefaultTarget) + "; break;\n"
        "switch(" + doValue(s.getKey) + ") {\n" + out + "}"
      case s: GotoStmt => "goto " + labels(s.getTarget) + ";"
      case s: ThrowStmt => "throw(" + doValue(s.getOp) + ");"
      case s: InvokeStmt => doExpr(s.getInvokeExpr) + ";"
      case s: EnterMonitorStmt => "RuntimeEnterMonitor(" + doValue(s.getOp) + ")"
      case s: ExitMonitorStmt => "RuntimeExitMonitor(" + doValue(s.getOp) + ")"
      case s: NopStmt => s";"
    }
    //unit.addBoxPointingToThis()
    //println("  unit:" + unit)
  }

  def doValue(value: Value): String = {
    value match {
      case t: Local => allocateLocal(t)
      case t: Immediate =>
        t match {
          case c: NullConstant => "NULL"
          case c: StringConstant => "cstr_to_JavaString(L\"" + escapeString(c.value) + "\")"
          case _ => t.toString
        }
      case t: ThisRef => "this"
      case t: ParameterRef => getParamName(t.getIndex)
      case t: CaughtExceptionRef =>
        referenceType(t.getType)
        //"((void*)(__caughtexception))"
        //"((" + mangler.typeToStringRef(t.getType) + ")(__caughtexception))"
        "__caughtexception"
      case t: ArrayRef =>
        doValue(t.getBase) + "->get(" + doValue(t.getIndex) + ")"
      case t: InstanceFieldRef =>
        referenceType(t.getField.getDeclaringClass)
        doValue(t.getBase) + "->" + t.getField.getName
      case t: StaticFieldRef =>
        referenceType(t.getField.getDeclaringClass)
        mangler.mangle(t.getField.getDeclaringClass) + "::" + t.getField.getName
      case t: Expr => doExpr(t)
      //case _ => value.toString
    }
  }

  private def escapeString(str: String): String = {
    str.map(v => v match {
      case '"' => "\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => v
    }).mkString("")
  }

  def doExpr(expr: Expr): String = {
    expr match {
      case e: BinopExpr =>
        doBinop(e.getType, e.getOp1, e.getOp2, e match {
          case k: AddExpr => "+"
          case k: AndExpr => "&"
          case k: DivExpr => "/"
          case k: RemExpr => "%"
          case k: MulExpr => "*"
          case k: OrExpr => "|"
          case k: ShlExpr => "<<"
          case k: ShrExpr => ">>"
          case k: UshrExpr => ">>>"
          case k: SubExpr => "-"
          case k: XorExpr => "^"
          case k: EqExpr => "=="
          case k: NeExpr => "!="
          case k: GeExpr => ">="
          case k: LeExpr => "<="
          case k: LtExpr => "<"
          case k: GtExpr => ">"
          case k: CmpExpr => "cmp"
          case k: CmplExpr => "cmpl"
          case k: CmpgExpr => "cmpg"
        })
      case e: CastExpr =>
        referenceType(e.getCastType)
        doCast(e.getType, e.getCastType, e.getOp)
      case e: InstanceOfExpr =>
        referenceType(e.getType)
        doInstanceof(e.getType, e.getCheckType, e.getOp)
      case e: NewExpr =>
        referenceType(e.getType)
        "new " + mangler.typeToStringNoRef(e.getType) + "()"
      case e: NewArrayExpr =>
        referenceType(e.getType)
        "new " + mangler.typeToStringNoRef(e.getType) + "(" + doValue(e.getSize) + ")"
      case e: NewMultiArrayExpr =>
        referenceType(e.getType)
        "new " + mangler.typeToStringNoRef(e.getType) + (0 to e.getSizeCount - 1).map(i => "[" + e.getSize(i) + "]").mkString
      case e: InvokeExpr =>
        referenceType(e.getMethod.getDeclaringClass)
        val args = e.getArgs.asScala.map(i => doValue(i))
        for (arg <- e.getArgs.asScala) {
          referenceType(arg.getType)
        }
        val argsCall = args.mkString(", ")
        e match {
          case i: StaticInvokeExpr => mangler.mangleFullName(e.getMethod) + "(" + argsCall + ")"
          case i: InstanceInvokeExpr =>
            i match {
              case i: InterfaceInvokeExpr =>
                doValue(i.getBase) + "->" + mangler.mangleBaseName(e.getMethod) + "(" + argsCall + ")"
              case i: VirtualInvokeExpr => doValue(i.getBase) + "->" + mangler.mangleBaseName(e.getMethod) + "(" + argsCall + ")"
              case i: SpecialInvokeExpr =>
                doValue(i.getBase) + "->" + mangler.mangle(i.getMethod.getDeclaringClass) + "::" + mangler.mangleBaseName(e.getMethod) + "(" + argsCall + ")"
            }
        }
      case e: LengthExpr => "((" + doValue(e.getOp) + ")->size())"
      case e: NegExpr => "-(" + doValue(e.getOp) + ")"
    }
  }

  def doCast(fromType:Type, toType:Type, value:Value): String = {
    "((" + mangler.typeToStringRef(toType) + ")" + doValue(value) + ")"
  }

  def doInstanceof(baseType:Type, checkType:Type, value:Value): String = {
    doValue(value) + " instanceof " + mangler.typeToStringRef(checkType)
  }

  def doBinop(kind:Type, left:Value, right:Value, op:String): String = {
    val l = doValue(left)
    val r = doValue(right)
    op match {
      case "cmp" | "cmpl" | "cmpg" => s"$op($l, $r)"
      case _ => s"$l $op $r"
    }
  }

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

  def doVariableAllocation(kind:Type, name:String) = mangler.typeToStringRef(kind) + " " + name + ";\n"

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
