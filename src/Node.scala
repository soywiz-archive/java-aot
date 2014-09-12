import org.objectweb.asm.{Type, Label}

import scala.collection.mutable.ListBuffer

class Var(val kind:Type, val name:String, val index:Int, val start:Int, val end:Int, val isArgument:Boolean) {}

abstract class Node
abstract class Expr() extends Node
abstract class LValue() extends Expr
abstract class Stm() extends Node

case class VoidExpr() extends Expr
case class VarExpr(variable: Var) extends LValue
case class TempExpr(index: Int) extends LValue
case class FieldAccessExpr(base: Expr, fieldName: String, fieldDesc: String = "") extends LValue
case class TypeRefExpr(name: String) extends Expr
case class MethodCallExpr(className: String, methodName: String, methodType: String, thisExpr: Expr, args: Array[Expr]) extends Expr
case class NewExpr(kind:Type) extends Expr
case class NewArrayExpr(countExpr:Expr, kind:Type) extends Expr
case class CheckCastExpr(expr:Expr, kind:Type) extends Expr
case class InstanceofExpr(expr:Expr, kind:Type) extends Expr
case class ConstExpr(value:Any) extends Expr
case class ArrayAccessExpr(expr:Expr, index:Expr) extends LValue
case class AssignTemp(index:Int, expr:Expr) extends Expr
case class BinOp(left:Expr, right:Expr, op:String) extends Expr
case class CastExpr(expr: Expr, from: Type, to: Type) extends Expr

case class ReturnStm(expr: Expr) extends Stm
case class ExprStm(expr: Expr) extends Stm
case class AssignStm(lvalue: LValue, expr: Expr) extends Stm
case class StmList(nodes: ListBuffer[Stm] = new ListBuffer[Stm]()) extends Stm
case class LineNumberStm(line:Int) extends Stm
case class LabelStm(label:Label) extends Stm
case class JumpIfStm(expr:Expr, label:Label) extends Stm
case class JumpStm(label:Label) extends Stm
