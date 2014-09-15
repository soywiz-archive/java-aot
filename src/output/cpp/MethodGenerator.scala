package output.cpp

import soot._
import soot.jimple._

import scala.collection.JavaConverters._
import scala.collection.mutable

class MethodGenerator(method:SootMethod) {
  private var locals = new mutable.HashMap[Local, String]
  private var lastLocalId = 0
  
  private val labels = new mutable.HashMap[Unit, String]
  private var lastLabelIndex = 0

  private val tryList = new mutable.HashMap[Unit, SootClass]
  private val catchList = new mutable.HashMap[Unit, SootClass]
  private val endCatchList = new mutable.HashMap[Unit, SootClass]
  private val referencedClasses = new mutable.HashSet[SootClass]

  def doMethod(): MethodResult = {
    val returnType = Mangling.typeToCppRef(method.getReturnType)
    val mangledFullName = mangleFullName(method)
    val mangledBaseName = mangleBaseName(method)
    val params = (0 to method.getParameterCount - 1).map(index => Mangling.typeToCppRef(method.getParameterType(index)) + " " + getParamName(index)).mkString(", ")
    val static = if (method.isStatic) "static" else ""
    var declaration = ""
    declaration += Mangling.visibility(method) + ": "
    if (method.isStatic) {
      declaration += "static "
    } else {
      //declaration += "virtual "
      if (method.isAbstract) {
        declaration += "virtual "
      }
    }
    declaration += s"$returnType $mangledBaseName($params)"
    if (method.isAbstract) {
      declaration += " = 0"
    }

    declaration += ";"

    for (argType <- method.getParameterTypes.asScala) {
      this.referenceType(argType.asInstanceOf[Type])
    }

    this.referenceType(method.getReturnType)

    if (method.isAbstract || method.isNative) return MethodResult(method, declaration, null, referencedClasses.toList)
    val body = method.retrieveActiveBody
    for (trap <- body.getTraps.asScala) {
      tryList(trap.getBeginUnit) = trap.getException
      catchList(trap.getHandlerUnit) = trap.getException
      endCatchList(trap.getEndUnit) = trap.getException
      //println(trap)
    }
    val units = body.getUnits

    getLabels(units.asScala.toList)

    var stms = ""
    for (unit <- units.asScala) {
      stms += this.doUnit(unit) + "\n"
    }

    var bodyString = ""
    for (local2 <- locals) {
      val (local, name) = local2
      bodyString += Mangling.typeToCppRef(local.getType) + " " + name + ";\n"
    }
    bodyString += stms

    MethodResult(method, declaration, s"$returnType $mangledFullName($params) { $bodyString }", referencedClasses.toList)
  }

  def getParamName(index:Int) = s"p$index"

  def allocateLabelName: String = {
    val res = "label_" + lastLabelIndex
    lastLabelIndex += 1
    res
  }

  def allocateLocal(t:Local): String = {
    if (!locals.contains(t)) {
      locals.put(t, "local" + lastLocalId)
      lastLocalId += 1
    }
    locals.get(t).get
  }

  def getLabels(units:List[Unit]): scala.Unit = {
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

  def doUnit(unit:soot.Unit): String = {
    var out = ""
    if (tryList.contains(unit)) out += "try {\n"
    if (catchList.contains(unit)) out += "} catch (" + Mangling.mangle(catchList(unit)) + " __caughtexception) {\n"
    if (endCatchList.contains(unit)) out += "}\n"
    if (labels.contains(unit)) out += labels(unit) + ":; "
    out += _doUnit(unit)
    out
  }

  def _doUnit(unit:soot.Unit): String = {
    var stms = ""
    unit match {
      case s:DefinitionStmt => doValue(s.getLeftOp) + " = " + doValue(s.getRightOp) + ";"
      case s:ReturnStmt => "return " + doValue(s.getOp) + ";"
      case s:ReturnVoidStmt => "return;"
      case s:IfStmt => s"if (" + doValue(s.getCondition) + ") { goto " + labels(s.getTarget) + "; }"
      case s:LookupSwitchStmt =>
        var out = ""
        var index = 0
        for (i <- 0 to s.getTargetCount - 1) {
          out += "case " + s.getLookupValue(i) + ": goto " + labels(s.getTarget(i)) + "; break;\n"
        }
        out += "default: goto " + labels(s.getDefaultTarget) + "; break;\n"
        "switch(" + doValue(s.getKey) + ") {\n" + out + "}"
      case s:TableSwitchStmt =>
        var out = ""
        var index = 0
        for (i <- s.getLowIndex to s.getHighIndex) {
          out += "case " + i + ": goto " + labels(s.getTarget(index)) + "; break;\n"
          index += 1
        }
        out += "default: goto " + labels(s.getDefaultTarget) + "; break;\n"
        "switch(" + doValue(s.getKey) + ") {\n" + out + "}"
      case s:GotoStmt => "goto " + labels(s.getTarget) + ";"
      case s:ThrowStmt => "throw(" + doValue(s.getOp) + ");"
      case s:InvokeStmt => doExpr(s.getInvokeExpr) + ";"
      case s:EnterMonitorStmt => "RuntimeEnterMonitor(" + doValue(s.getOp) + ")"
      case s:ExitMonitorStmt => "RuntimeExitMonitor(" + doValue(s.getOp) + ")"
      case s:NopStmt => s";"
    }
    //unit.addBoxPointingToThis()
    //println("  unit:" + unit)
  }

  def doValue(value:Value):String = {
    value match {
      case t: Local => allocateLocal(t)
      case t: Immediate =>
        t match {
          case c:NullConstant => "NULL"
          case c:StringConstant => "cstr_to_JavaString(L\"" + escapeString(c.value) + "\")"
          case _=> t.toString()
        }
      case t: ThisRef => "this"
      case t: ParameterRef => getParamName(t.getIndex)
      case t: CaughtExceptionRef => "__caughtexception"
      case t: ArrayRef =>
        doValue(t.getBase) + "->get(" + doValue(t.getIndex) + ")"
      case t: InstanceFieldRef =>
        referenceType(t.getField.getDeclaringClass)
        doValue(t.getBase) + "->" + t.getField.getName
      case t: StaticFieldRef =>
        referenceType(t.getField.getDeclaringClass)
        Mangling.mangle(t.getField.getDeclaringClass) + "::" + t.getField.getName
      case t: Expr => doExpr(t)
      //case _ => value.toString
    }
  }

  private def escapeString(str:String): String = {
    str.map(v => v match {
      case '"' => "\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case _ => v
    }).mkString("")
  }

  def doExpr(expr:Expr):String = {
    expr match {
      case e:BinopExpr =>
        val op = e match {
          case z:AddExpr => "+"
          case z:AndExpr => "&"
          case z:DivExpr => "/"
          case z:RemExpr => "%"
          case z:MulExpr => "*"
          case z:OrExpr => "|"
          case z:ShlExpr => "<<"
          case z:ShrExpr => ">>"
          case z:UshrExpr => ">>>"
          case z:SubExpr => "-"
          case z:XorExpr => "^"
          case z:CmpExpr => "=="

          case z:CmplExpr => "<"
          case z:CmpgExpr => ">"

          case z:EqExpr => "=="
          case z:NeExpr => "!="
          case z:GeExpr => ">="
          case z:LeExpr => "<="
          case z:LtExpr => "<"
          case z:GtExpr => ">"
        }
        doValue(e.getOp1) + " " + op + " " + doValue(e.getOp2)
      case e:CastExpr =>
        referenceType(e.getCastType)
        "((" + Mangling.typeToCppRef(e.getCastType) + ")" + doValue(e.getOp) + ")"
      case e:InstanceOfExpr =>
        referenceType(e.getType)
        e.getOp + " instanceof " + Mangling.typeToCppRef(e.getType)
      case e:NewExpr =>
        referenceType(e.getType)
        "new " + Mangling.typeToCppNoRef(e.getType) + "()"
      case e:NewArrayExpr =>
        referenceType(e.getType)
        "new " + Mangling.typeToCppNoRef(e.getType) + "(" + doValue(e.getSize) + ")"
      case e:NewMultiArrayExpr =>
        referenceType(e.getType)
        "new " + Mangling.typeToCppNoRef(e.getType) + (0 to e.getSizeCount - 1).map(i => "[" + e.getSize(i) + "]").mkString
      case e:InvokeExpr =>
        referenceType(e.getMethod.getDeclaringClass)
        val args = e.getArgs.asScala.map(i => doValue(i))
        for (arg <- e.getArgs.asScala) {
          referenceType(arg.getType)
        }
        val argsCall = args.mkString(", ")
        e match {
          case i:StaticInvokeExpr => mangleFullName(e.getMethod) + "(" + argsCall + ")"
          case i:InstanceInvokeExpr =>
            i match {
              case i:InterfaceInvokeExpr =>
                doValue(i.getBase) + "->" + mangleBaseName(e.getMethod) + "(" + argsCall + ")"
              case i:VirtualInvokeExpr => doValue(i.getBase) + "->" + mangleBaseName(e.getMethod) + "(" + argsCall + ")"
              case i:SpecialInvokeExpr =>
                doValue(i.getBase) + "->" + Mangling.mangle(i.getMethod.getDeclaringClass) + "::" + mangleBaseName(e.getMethod) + "(" + argsCall + ")"
            }
        }
      case e:LengthExpr => "((" + doValue(e.getOp) + ")->size())"
      case e:NegExpr => "-(" + doValue(e.getOp) + ")"
    }
  }

  def referenceType(kind:Type):scala.Unit = {
    kind match {
      case r:ArrayType => referenceType(r.getElementType)
      case r:RefType => referenceType(r.getSootClass)
      case _ =>
    }
  }

  def referenceType(clazz:SootClass):scala.Unit = {
    referencedClasses.add(clazz)
  }

  def mangleBaseName(method:SootMethod): String = {
    val name = method.getName
    name.replace('.', '_').replace('(', '_').replace(')', '_').replace("<", "__").replace(">", "__").replace(" ", "")
  }

  def mangleFullName(method:SootMethod): String = {
    val name = method.getDeclaringClass.getName + "::" + method.getName
    name.replace('.', '_').replace('(', '_').replace(')', '_').replace("<", "__").replace(">", "__").replace(" ", "")
  }
}

