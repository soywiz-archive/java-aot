package ast

import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.Type

abstract class NodeType()
case class VoidType() extends NodeType()
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
case class ClassType(name:String) extends NodeType()
case class MethodType(retval:NodeType, arguments:List[NodeType]) extends NodeType() {
  lazy val hasReturnValue = !retval.isInstanceOf[VoidType]
}

case class LabelRef(index:Int)
case class FieldRef(owner:ClassType, kind:NodeType, name:String) { def getType = kind; }
case class MethodRef(owner:ClassType, kind:MethodType, name:String) { def getType = kind; }

object NodeUtils {
  def typeFromDesc(desc:String): NodeType = typeFromType(Type.getType(desc))
  def typeFromType(desc:Type): NodeType = {
    desc.getSort match {
      case Type.VOID => VoidType()
      case Type.BOOLEAN => BoolType()
      case Type.BYTE => ByteType()
      case Type.SHORT => ShortType()
      case Type.CHAR => CharType()
      case Type.INT => IntType()
      case Type.LONG => LongType()
      case Type.FLOAT => FloatType()
      case Type.DOUBLE => DoubleType()
      case Type.ARRAY => ArrayType(typeFromType(desc.getElementType))
      case Type.OBJECT => ClassType(desc.getClassName)
      case Type.METHOD => MethodType(typeFromType(desc.getReturnType), desc.getArgumentTypes.map(typeFromType).toList)
    }
  }
}

//def StringType() = ClassType("java.lang.String")

abstract class Expr() { def getType:NodeType; }

abstract class LValue() extends Expr()
case class Local(kind:NodeType, index:Int = -1, name:String = "") extends LValue() { val getType = kind; }
case class This(kind:NodeType) extends LValue() { val getType = kind; }
//case class Argument(kind:NodeType, index:Int) extends LValue() { val getType = kind; }
case class ArrayAccess(items:(Expr, Expr)) extends LValue() { lazy val getType = items._1.getType; }
case class Field(field:FieldRef, instance:Expr) extends LValue() { val getType = field.getType; }
case class StaticField(field:FieldRef) extends LValue() { val getType = field.getType; }

case class Binop(op:String, items:(Expr, Expr)) extends Expr() {
  lazy val getType = {
    op match {
      case "==" | "!=" | ">=" | "<=" | ">" | "<" => BoolType()
      case "cmp" | "cmpl" | "cmpg" => IntType()
      case _ => assert(items._1.getType == items._2.getType); items._1.getType
    }
  }
}
case class Unop(op:String, r:Expr) extends Expr() { lazy val getType = r.getType; }
case class Conv(v:Expr, toType:NodeType) extends Expr() { lazy val getType = toType; }
case class ArrayLength(v:Expr) extends Expr() { val getType = IntType(); }
case class CheckCast(kind:ClassType, v:Expr) extends Expr() { val getType = kind; }
case class InstanceOf(kind:ClassType, v:Expr) extends Expr() { val getType = BoolType(); }
case class New(kind:ClassType) extends Expr() { val getType = kind; }
case class NewArray(kind:NodeType, count:Expr) extends Expr() { val getType = kind; }

abstract class Constant() extends Expr()
case class NullConstant() extends Constant() { val getType = NullType(); }
case class BoolConstant(v:Boolean) extends Constant() { val getType = BoolType(); }
case class IntConstant(v:Int) extends Constant() { val getType = IntType(); }
case class LongConstant(v:Long) extends Constant() { val getType = LongType(); }
case class FloatConstant(v:Float) extends Constant() { val getType = FloatType(); }
case class DoubleConstant(v:Double) extends Constant() { val getType = DoubleType(); }
case class StringConstant(v:String) extends Constant() { val getType = ClassType("java.lang.String"); }
case class ClassConstant(v: ClassType) extends Constant() { val getType = v; }

case class Invoke(methodRef:MethodRef, args:Seq[Expr]) extends Expr() { val getType = methodRef.kind.retval; }

abstract class Stm()
case class BranchStm(cond:Expr, label:LabelRef) extends Stm()
case class JumpStm(label:LabelRef) extends Stm()
case class ExprStm(v:Expr) extends Stm()
case class ReturnStm(cond:Expr) extends Stm()
case class ReturnVoidStm() extends Stm()
case class ThrowStm(cond:Expr) extends Stm()
case class MonitorEnter(cond:Expr) extends Stm()
case class MonitorExit(cond:Expr) extends Stm()
case class Assign(value:LValue, v:Expr) extends Stm()
case class LabelStm(labelNode:LabelRef) extends Stm()