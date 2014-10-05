package target.cpp

import soot._
import soot.jimple.{Expr, ArrayRef, Stmt}
import target.base.BaseMethodBodyGenerator

import scala.collection.mutable

class CppMethodBodyGenerator(method:SootMethod) extends BaseMethodBodyGenerator(method, CppMangler) {
  override def doInstanceof(baseType:Type, checkType:Type, value:Value): String = {
    doValue(value) + " instanceof " + mangler.typeToStringRef(checkType)
  }

  override def doStaticField(kind:SootClass, fieldName:String): String = {
    mangler.mangle(kind) + "::" + fieldName
  }

  override def doBinop(kind:Type, left:Value, right:Value, op:String): String = {
    var l = doValue(left)
    var r = doValue(right)
    if (mangler.isRefType(left.getType)) l = s"$l.get()"
    if (mangler.isRefType(right.getType)) r = s"$r.get()"
    op match {
      case "cmp" | "cmpl" | "cmpg" => s"$op($l, $r)"
      case _ =>
        s"$l $op $r"
    }
  }

  override def doCast(fromType:Type, toType:Type, value:Value): String = {
    //"((" + mangler.typeToStringRef(toType) + ")" + doValue(value) + ")"
    if (mangler.isRefType(toType)) {
      "(std::dynamic_pointer_cast< " + mangler.typeToStringNoRef(toType) + " >(" + doValue(value) + "))"
    } else {
      "((" + mangler.typeToStringRef(toType) + ")" + doValue(value) + ")"
    }
  }

  override def doNew(kind:Type): String = {
    val newType = mangler.typeToStringNoRef(kind)
    s"std::shared_ptr< $newType >(new $newType())"
  }

  override def doLocal(localName: String): String = localName
  override def doParam(paramName: String): String = paramName
  override def doInstanceField(instance: Value, fieldName: String): String = doValue(instance) + "->" + fieldName

  // negate is - or ~
  override def doNegate(value:Value):String = "-(" + doValue(value) + ")"
  override def doLength(value:Value):String = "((" + doValue(value) + ")->size())"
  override def doStringLiteral(s: String): String = "cstr_to_JavaString(L\"" + escapeString(s) + "\")"
  override def doVariableAllocation(kind:Type, name:String):String = {
    mangler.typeToStringRef(kind) + " " + name + ";\n"
  }

  override def doNewArray(kind: Type, size: Value): String = {
    "std::shared_ptr< " + mangler.typeToStringNoRef(kind) + " >(new " + mangler.typeToStringNoRef(kind) + "(" + doValue(size) + "))"
  }

  override def doNewMultiArray(kind: Type, values: Array[Value]): String = {
    "new " + mangler.typeToStringNoRef(kind) + values.map(i => "[" + doValue(i) + "]").mkString
  }

  override def doNop(): String = s";"
  override def doGoto(unit: Unit): String = "goto " + labels(unit) + ";"
  override def doReturn(returnType: Type, returnValue: Value): String = "return " + doCastIfNeeded(returnType, returnValue) + ";"
  override def doReturnVoid() = "return;"
  override def doCaughtException(value: Type): String = {
    //"((void*)(__caughtexception))"
    //"((" + mangler.typeToStringRef(t.getType) + ")(__caughtexception))"
    "__caughtexception"
  }

  override def doArrayAccess(value: Value, value1: Value): String = {
    val base = doValue(value)
    val index = doValue(value1)
    s"$base->get($index)"
    //"(" + mangler.typeToStringRef(t.getType) + ")(void *)(" + doValue(t.getBase) + "->get(" + doValue(t.getIndex) + "))"
  }

  override def doIf(condition: Value, target: Stmt): String = {
    s"if (" + doValue(condition) + ") { goto " + labels(target) + "; }"
  }

  override def doConstantClass(s: String): String = "NULL /* ClassConstant:" + s + "*/"
  override def doConstantNull(): String = "std::shared_ptr<void*>(NULL)"
  override def doConstantInt(value: Int): String = s"${value}"
  override def doConstantLong(value: Long): String = s"${value}L"
  override def doConstantFloat(value: Float): String = s"${value}f"
  override def doConstantDouble(value: Double): String = s"${value}"
  override def doConstantString(value: String): String = escapeString(value)

  override def doThrow(value: Value): String = "throw(" + doValue(value) + ");"
  override def doLabel(labelName: String): String = s"$labelName:; "
  override def doTryStart():String = "try {\n"
  override def doCatchAndGoto(trapType: SootClass, labelName: String): String = {
    "} catch (std::shared_ptr<" + mangler.mangle(trapType) + s"> __caughtexception__) { __caughtexception = __caughtexception__; goto $labelName; }\n"
  }
  override def doSwitch(matchValue:Value, defaultLabel: String, map: mutable.HashMap[Int, String]): String = {
    def matchValueString = doValue(matchValue)
    def defaultCase = s"default: goto $defaultLabel; break;\n"
    def cases = {
      map.map(v => {
        val(key, label) = v
        s"case $key: goto $label; break;\n"
      }).mkString
    }

    s"switch ($matchValueString) { $cases $defaultCase }"
  }

  override def doAssign(leftOp: Value, rightOp: Value): String = {
    // l-value
    leftOp match {
      case s: ArrayRef =>
        val base = doValue(s.getBase)
        val index = doValue(s.getIndex)
        val right = doValue(rightOp)
        s"$base->set($index, $right);"
      case _ =>
        if (rightOp.getType.equals(leftOp.getType)) {
          doValue(leftOp) + " = " + doValue(rightOp) + ";"
        } else {
          // Exceptions for example!
          doValue(leftOp) + " = " + doCast(rightOp.getType, leftOp.getType, rightOp) + ";"
        }
    }
  }

  override def doExprStm(expr: Expr): String = doExpr(expr) + ";"

  override def doThisRef(clazz: SootClass): String = doWrapRefType(method.getDeclaringClass, "this")

  override def doEnterMonitor(value: Value): String = "RuntimeEnterMonitor(" + doValue(value) + ")"
  override def doExitMonitor(value: Value): String = "RuntimeExitMonitor(" + doValue(value) + ")"

  override def doInvokeStatic(method: SootMethod, args: Seq[String]): String = mangler.mangleFullName(method) + "(" + args.mkString + ")"

  override def doInvokeInstance(base: Value, method: SootMethod, args: List[String], special:Boolean): String = {
    val argsCall = args.mkString(", ")
    if (special) {
      doValue(base) + "->" + mangler.mangle(method.getDeclaringClass) + "::" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
    } else {
      if (method.getDeclaringClass.getName == "java.lang.Object") {
        // Required for interfaces not extending Object directly
        "((std::dynamic_pointer_cast<java_lang_Object>)(" + doValue(base) + "))->" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
      } else {
        doValue(base) + "->" + mangler.mangleBaseName(method) + "(" + argsCall + ")"
      }
    }
  }

  private def doWrapRefType(toType:SootClass, value: String): String = {
    "std::shared_ptr< " + mangler.mangle(toType) + " >(" + value + ")"
  }

  private def doWrapRefType(toType:Type, value: String): String = {
    "std::shared_ptr< " + mangler.typeToStringNoRef(toType) + " >(" + value + ")"
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
}
