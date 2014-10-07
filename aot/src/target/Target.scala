package target

import java.io.File
import java.nio.charset.Charset

import _root_.util._
import soot._
import soot.jimple.{TableSwitchStmt, _}
import vfs.VfsNode

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class Target {
  protected val cl = this.getClass.getClassLoader
  protected val utf8 = Charset.forName("UTF-8")
  protected val file_separator = System.getProperty("file.separator")

  def targetName:String

  def createClassContext(projectContext:BaseProjectContext, clazz:SootClass): BaseClassContext = {
    new BaseClassContext(projectContext, clazz)
  }

  def createProjectContext(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode): BaseProjectContext = {
    new BaseProjectContext(classNames, mainClass, runtimeProvider, outputPath)
  }

  def buildAndRun(classNames:Seq[String], mainClass:String, runtimeProvider:RuntimeProvider, outputPath:VfsNode): scala.Unit = {
    val projectContext = createProjectContext(classNames, mainClass, runtimeProvider, outputPath)
    generateProject(projectContext)
    buildProject(projectContext)
    runProject(projectContext)
  }

  def generateProject(projectContext:BaseProjectContext): scala.Unit = {
    // Load classes into stage
    projectContext.classNames.foreach(Scene.v.loadClassAndSupport)

    // Preprocesses classes
    projectContext.classNames.map(Scene.v.getSootClass).foreach(preprocessClass)

    for (className <- projectContext.classNames) {
      val clazz = Scene.v.getSootClass(className)
      println("Processing class: " + clazz.getName)
      generateClass(createClassContext(projectContext, clazz))
    }
    println("Processed classes: " + projectContext.classNames.length)
  }

  // @TODO
  /*
  class TargetMethod(clazz:TargetClass, method:SootMethod) {

  }

  class TargetClass(clazz:SootClass) {

  }
  */

  def preprocessClass(clazz:SootClass): scala.Unit = {

  }

  def buildProject(projectContext:BaseProjectContext): scala.Unit = {
  }
  
  def runProject(projectContext:BaseProjectContext): scala.Unit = {
  }

  protected def classNameToPath(name:String): String = name.replace(".", "/")

  def generateClass(classContext:BaseClassContext): scala.Unit = {
    val clazz = classContext.clazz

    if (clazz.hasSuperclass) classContext.referenceType(clazz.getSuperclass)
    for (interface <- clazz.getInterfaces.asScala) classContext.referenceType(interface)

    for (method <- clazz.getMethods.asScala) {
      val methodContext = new BaseMethodContext(classContext, method)
      methodContext.methodWithBody = doMethodWithBodyOrAbstract(methodContext)
      for (kind <- method.getParameterTypes.asScala.map(_.asInstanceOf[Type])) {
        classContext.referenceType(kind)
      }
      classContext.referenceType(method.getReturnType)
      for (clazz <- methodContext.referencedClasses) classContext.referenceType(clazz)
    }
  }

  def doMethodWithBodyOrAbstract(methodContext:BaseMethodContext): String = {
    val method = methodContext.method
    if (methodHasBody(method)) {
      doMethodWithBody(methodContext)
    } else {
      SootUtils.getTag(method.getTags.asScala, "Llibcore/MethodBody;", "value").asInstanceOf[String]
    }
  }

  def methodHasBody(method:SootMethod):Boolean = !method.isAbstract && !method.isNative

  def doMethodWithBody(context:BaseMethodContext): String

  final def doMethodBody(context:BaseMethodContext): String = {
    val body = context.method.retrieveActiveBody
    for (trap <- body.getTraps.asScala) {
      val trapId = context.lastTrapId
      context.lastTrapId += 1
      println(s"handler: ${trap.getBeginUnit}, ${trap.getHandlerUnit}, ${trap.getEndUnit}")
      if (!context.tryList.contains(trap.getBeginUnit)) context.tryList(trap.getBeginUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      if (!context.catchList.contains(trap.getHandlerUnit)) context.catchList(trap.getHandlerUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      if (!context.endCatchList.contains(trap.getEndUnit)) context.endCatchList(trap.getEndUnit) = new ListBuffer[Tuple2[Int, SootClass]]
      context.tryList(trap.getBeginUnit).append((trapId, trap.getException))
      context.catchList(trap.getHandlerUnit).append((trapId, trap.getException))
      context.endCatchList(trap.getEndUnit).append((trapId, trap.getException))
      //println(trap)
    }
    val units = body.getUnits

    context.processLabels(units.asScala.toList)

    var stms = ""
    for (unit <- units.asScala) {
      stms += this.doUnit(unit, context) + "\n"
    }

    var bodyString = "\n"
    if (context.usingExceptions) {
      bodyString += doVariableAllocation(RefType.v("java.lang.Throwable"), "__caughtexception", context)
    }
    for (local2 <- context.locals) {
      val (local, name) = local2
      bodyString += doVariableAllocation(local.getType, name, context)
    }
    bodyString += stms
    bodyString
  }


  final def doUnit(unit: soot.Unit, context:BaseMethodContext): String = {
    def processLabel = if (context.labels.contains(unit)) doLabel(context.labels(unit), context) else ""
    def processTry = if (context.tryList.contains(unit)) context.tryList(unit).map(_ => doTryStart(context)).mkString else ""
    def processEndCatch = {
      if (context.endCatchList.contains(unit)) {
        context.endCatchList(unit).map(item => {
          val (trapId, trapType) = item
          doCatchAndGoto(trapType, s"exception_handler_$trapId", context)
        }).mkString
      } else {
        ""
      }
    }
    def processExceptionHandler = {
      if (context.catchList.contains(unit)) {
        context.catchList(unit).map(item => {
          val (trapId, trapType) = item
          context.usingExceptions = true
          doLabel(s"exception_handler_$trapId", context) + "\n"
        }).mkString
      } else {
        ""
      }
    }
    def processUnit = _doUnit(unit, context)

    List(processLabel, processTry, processEndCatch, processExceptionHandler, processUnit).mkString
  }

  final def _doUnit(unit: soot.Unit, context:BaseMethodContext): String = {
    unit match {
      case s: DefinitionStmt => context.referenceType(s.getLeftOp.getType); doAssign(s.getLeftOp, s.getRightOp, context)
      case s: ReturnStmt => doReturn(context.method.getReturnType, s.getOp, context)
      case s: ReturnVoidStmt => doReturnVoid(context)
      case s: IfStmt => doIf(s.getCondition, s.getTarget, context)
      case s: GotoStmt => doGoto(s.getTarget, context)
      case s: ThrowStmt => doThrow(s.getOp, context)
      case s: InvokeStmt => doExprStm(s.getInvokeExpr, context)
      case s: EnterMonitorStmt => doEnterMonitor(s.getOp, context)
      case s: ExitMonitorStmt => doExitMonitor(s.getOp, context)
      case s: NopStmt => doNop(context)
      case s: LookupSwitchStmt =>
        doSwitch(s.getKey, context.labels(s.getDefaultTarget),
          CollectionUtils.uniqueMap((0 until s.getTargetCount).map(i => (s.getLookupValue(i), context.labels(s.getTarget(i))))).orNull, context
        )
      case s: TableSwitchStmt =>
        doSwitch(s.getKey, context.labels(s.getDefaultTarget),
          CollectionUtils.uniqueMap((s.getLowIndex to s.getHighIndex).map(i => (i, context.labels(s.getTarget(i - s.getLowIndex))))).orNull, context
        )
    }
    //unit.addBoxPointingToThis()
    //println("  unit:" + unit)
  }

  final def doValue(value: Value, context:BaseMethodContext): String = {
    value match {
      case t: Local => doLocal(context.allocateLocal(t), context)
      case t: Immediate => doImmediate(t, context)
      case t: ThisRef => doThisRef(context.method.getDeclaringClass, context)
      case t: ParameterRef => doParam(context.getParamName(t.getIndex), context)
      case t: CaughtExceptionRef => context.referenceType(t.getType); doCaughtException(t.getType, context)
      case t: ArrayRef => doArrayAccess(t.getBase, t.getIndex, context)
      case t: InstanceFieldRef => context.referenceType(t.getField.getDeclaringClass); doInstanceField(t.getBase, t.getField.getName, context)
      case t: StaticFieldRef => context.referenceType(t.getField.getDeclaringClass); doStaticField(t.getField.getDeclaringClass, t.getField.getName, context)
      case t: Expr => doExpr(t, context)
    }
  }

  final def doImmediate(i: Immediate, context:BaseMethodContext): String = {
    i match {
      case c: NullConstant => doConstantNull(context)
      case c: IntConstant => doConstantInt(c.value, context)
      case c: LongConstant => doConstantLong(c.value, context)
      case c: FloatConstant => doConstantFloat(c.value, context)
      case c: DoubleConstant => doConstantDouble(c.value, context)
      case c: StringConstant => doConstantString(c.value, context)
      case c: ClassConstant => doConstantClass(c.value, context)
    }
  }

  final def doExpr(expr: Expr, context:BaseMethodContext): String = {
    expr match {
      case e: CastExpr => context.referenceType(e.getCastType); doCast(e.getOp.getType, e.getCastType, e.getOp, context)
      case e: InstanceOfExpr => context.referenceType(e.getType); doInstanceof(e.getType, e.getCheckType, e.getOp, context)
      case e: NewExpr => context.referenceType(e.getType); doNew(e.getType, context)
      case e: NewArrayExpr => context.referenceType(e.getType.getArrayType); doNewArray(e.getType, e.getSize, context)
      case e: NewMultiArrayExpr => context.referenceType(e.getType); doNewMultiArray(e.getType, (0 to e.getSizeCount - 1).map(e.getSize).toArray, context)
      case e: LengthExpr => doLength(e.getOp, context)
      case e: NegExpr => doNegate(e.getOp, context)
      case e: BinopExpr => doBinop(e.getType, e.getOp1, e.getOp2, getOpString(e), context)
      case e: InvokeExpr =>
        context.referenceType(e.getMethod.getDeclaringClass)
        val argsList = e.getArgs.asScala.toList
        val castTypes = e.getMethod.getParameterTypes.asScala.map(_.asInstanceOf[Type]).toList
        val args = (argsList, castTypes).zipped.map((value, expectedType) => doCastIfNeeded(expectedType, value, context)).toList
        e.getArgs.asScala.foreach(i => context.referenceType(i.getType))
        e match {
          case i: StaticInvokeExpr => doInvokeStatic(e.getMethod, args, context)
          case i: InstanceInvokeExpr => doInvokeInstance(i.getBase, e.getMethod, args, i.isInstanceOf[SpecialInvokeExpr], context)
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

  final def doCastIfNeeded(toType:Type, value:Value, context:BaseMethodContext): String = {
    if (value.getType.equals(toType)) {
      doValue(value, context)
    } else {
      doCast(value.getType, toType, value, context)
    }
  }

  def doCast(fromType:Type, toType:Type, value:Value, context:BaseMethodContext): String
  def doInstanceof(baseType:Type, checkType:Type, value:Value, context:BaseMethodContext): String
  def doBinop(kind:Type, left:Value, right:Value, op:String, context:BaseMethodContext): String
  def doNegate(value:Value, context:BaseMethodContext):String
  def doLength(value:Value, context:BaseMethodContext):String
  def doStaticField(kind:SootClass, fieldName:String, context:BaseMethodContext): String
  def doNew(kind:Type, context:BaseMethodContext): String
  def doVariableAllocation(kind:Type, name:String, context:BaseMethodContext): String
  def doNewArray(kind: Type, size: Value, context:BaseMethodContext): String
  def doNewMultiArray(kind: Type, values: Array[Value], context:BaseMethodContext): String
  def doNop(context:BaseMethodContext): String
  def doCaughtException(value: Type, context:BaseMethodContext): String
  def doArrayAccess(value: Value, value1: Value, context:BaseMethodContext): String
  def doGoto(unit: Unit, context:BaseMethodContext): String
  def doReturn(returnType: Type, returnValue: Value, context:BaseMethodContext): String
  def doReturnVoid(context:BaseMethodContext): String
  def doIf(condition: Value, target: Stmt, context:BaseMethodContext): String
  def doConstantClass(s: String, context:BaseMethodContext): String
  def doConstantNull(context:BaseMethodContext): String
  def doConstantInt(value: Int, context:BaseMethodContext): String
  def doConstantLong(value: Long, context:BaseMethodContext): String
  def doConstantFloat(value: Float, context:BaseMethodContext): String
  def doConstantDouble(value: Double, context:BaseMethodContext): String
  def doConstantString(value: String, context:BaseMethodContext): String
  def doThrow(value: Value, context:BaseMethodContext): String
  def doLabel(labelName: String, context:BaseMethodContext): String
  def doTryStart(context:BaseMethodContext):String
  def doCatchAndGoto(trapType: SootClass, labelName: String, context:BaseMethodContext): String
  def doSwitch(matchValue:Value, defaultLabel: String, map: mutable.HashMap[Int, String], context:BaseMethodContext): String
  def doAssign(leftOp: Value, rightOp: Value, context:BaseMethodContext): String
  def doExprStm(expr: Expr, context:BaseMethodContext): String
  def doThisRef(clazz: SootClass, context:BaseMethodContext): String
  def doEnterMonitor(value: Value, context:BaseMethodContext): String
  def doExitMonitor(value: Value, context:BaseMethodContext): String
  def doLocal(localName: String, context:BaseMethodContext): String
  def doParam(paramName: String, context:BaseMethodContext): String
  def doInstanceField(instance: Value, fieldName: String, context:BaseMethodContext): String
  def doInvokeStatic(method: SootMethod, args: Seq[String], context:BaseMethodContext): String
  def doInvokeInstance(base: Value, method: SootMethod, args: List[String], special:Boolean, context:BaseMethodContext): String
}
