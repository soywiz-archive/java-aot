package ast

import com.sun.org.apache.xpath.internal.operations.Bool
import org.objectweb.asm.tree.LabelNode

case class NodeType()
case class NullType() extends NodeType()
case class BoolType() extends NodeType()
case class ByteType() extends NodeType()
case class CharType() extends NodeType()
case class ShortType() extends NodeType()
case class IntType() extends NodeType()
case class LongType() extends NodeType()
case class FloatType() extends NodeType()
case class DoubleType() extends NodeType()
case class ArrayType(element:NodeType) extends NodeType()
case class ClassType(desc:String) extends NodeType()

object NodeUtils {
  def typeFromDesc(desc:String) = ClassType(desc)
}

//def StringType() = ClassType("java.lang.String")

abstract case class Expr() { def getType:NodeType; }

abstract case class FieldRef(owner:String, name:String, desc:ClassType) { def getType = desc; }
abstract case class LValue() extends Expr()
case class Local(kind:NodeType, index:Int = -1) extends LValue() { val getType = kind; }
case class ArrayAccess(items:(Expr, Expr)) extends LValue() { lazy val getType = items._1.getType; }
case class Field(field:FieldRef, instance:Expr = null) extends LValue() { val getType = field.getType; }

case class Binop(op:String, items:(Expr, Expr)) extends Expr() {
  lazy val getType = {
    op match {
      case "==" | "!=" | ">=" | "<=" | ">" | "<" => classOf[Bool]
      case "cmp" | "cmpl" | "cmpg" => classOf[Int]
      case _ => assert(items._1.getType == items._2.getType); items._1.getType
    }
  }
}
case class Unop(op:String, r:Expr) extends Expr() { lazy val getType = r.getType; }
case class Conv[T](v:Expr, toType:Class[T]) extends Expr() { lazy val getType = toType; }
case class ArrayLength(v:Expr) extends Expr() { val getType = classOf[Int]; }
case class CheckCast(kind:ClassType, v:Expr) extends Expr() { val getType = kind; }
case class InstanceOf(kind:ClassType, v:Expr) extends Expr() { val getType = classOf[Bool]; }
case class New(kind:ClassType) extends Expr() { val getType = kind; }
case class NewArray(kind:ClassType, count:Expr) extends Expr() { val getType = kind; }

abstract case class Constant() extends Expr()
case class NullConstant() extends Constant() { val getType = NullType(); }
case class BoolConstant(v:Boolean) extends Constant() { val getType = BoolType(); }
case class IntConstant(v:Int) extends Constant() { val getType = IntType(); }
case class LongConstant(v:Long) extends Constant() { val getType = LongType(); }
case class FloatConstant(v:Float) extends Constant() { val getType = FloatType(); }
case class DoubleConstant(v:Double) extends Constant() { val getType = DoubleType(); }
case class StringConstant(v:String) extends Constant() { val getType = ClassType("java.lang.String"); }
case class ClassConstant(v: ClassType) extends Constant() { val getType = v; }

abstract case class Stm()
case class GotoStm(cond:Expr, label:LabelNode) extends Stm()
case class ReturnStm(cond:Expr = null) extends Stm()
case class ThrowStm(cond:Expr) extends Stm()
case class MonitorEnter(cond:Expr) extends Stm()
case class MonitorExit(cond:Expr) extends Stm()
case class Assign(value:LValue, v:Expr) extends Stm()
