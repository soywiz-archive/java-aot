import java.io.{File, FileInputStream}

import org.objectweb.asm.tree._
import org.objectweb.asm.{Label, ClassReader, Opcodes, Type}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Main extends App {
  //handleClass(new File("target/classes/Main.class"))
  handleClass(new File("target/classes/Test1.class"))

  val result = Test1.sum(1, 2)

  def handleClass(file: File) = {
    val cr = new ClassReader(new FileInputStream(file))
    val cn = new ClassNode()
    cr.accept(cn, 0)
    for (_method <- cn.methods.asScala) {
      val method = _method.asInstanceOf[MethodNode]
      println("-----------------------")
      println(s"${method.name}_${method.desc}")
      handleMethod(method)
    }
  }

  def handleMethod(method: MethodNode) = {
    var statements = new StmList()
    var stack = new mutable.Stack[Expr]()
    var tempindex = 0

    if (method.instructions.size > 0) {
      for (_instruction <- method.instructions.iterator().asScala) {
        //println(_instruction)

        //val instruction = _instruction.asInstanceOf[AbstractInsnNode]
        _instruction match {
          case field: FieldInsnNode =>
            field.getOpcode match {
              case Opcodes.GETSTATIC =>
                //throw new Exception("Not implemented GETSTATIC")
                stack.push(new FieldAccessExpr(new TypeRefExpr(field.owner), field.name))

              case Opcodes.PUTSTATIC =>
                val value = stack.pop()
                statements.nodes.append(new AssignStm(new FieldAccessExpr(new TypeRefExpr(field.owner), field.name), value))

              case Opcodes.GETFIELD =>
                val ref = stack.pop()
                stack.push(new FieldAccessExpr(ref, field.name))

              case Opcodes.PUTFIELD =>
                val ref = stack.pop()
                val value = stack.pop()
                statements.nodes.append(new AssignStm(new FieldAccessExpr(ref, field.name), value))
            }
          //println(s"FIELD: ${field.owner}.${field.name} :: ${field.desc}")

          case method: MethodInsnNode =>
            var isStatic = false
            method.getOpcode match {
              case Opcodes.INVOKEVIRTUAL => isStatic = false
              case Opcodes.INVOKESPECIAL => isStatic = false
              case Opcodes.INVOKESTATIC => isStatic = true
              case Opcodes.INVOKEINTERFACE => isStatic = false
            }
            val methodType = Type.getMethodType(method.desc)
            val argumentTypes = methodType.getArgumentTypes
            val argumentExprs = (for (n <- argumentTypes) yield stack.pop()).reverse.toArray
            val thisExpr = if (!isStatic) {
              stack.pop()
            } else {
              new TypeRefExpr(method.owner)
            }
            stack.push(new MethodCall(method.owner, method.name, method.desc, thisExpr, argumentExprs.toArray))
            if (methodType.getReturnType.getSort == Type.VOID) {
              statements.nodes.append(new ExprStm(stack.pop()))
            }

          //methodType.getReturnType
          //method.desc
          //method.itf
          //println(s"CALL: ${method.owner}.${method.name} :: ${method.desc}")

          case varn: VarInsnNode =>
            var loading = false
            varn.getOpcode match {
              case Opcodes.ILOAD | Opcodes.LLOAD | Opcodes.FLOAD | Opcodes.DLOAD | Opcodes.ALOAD => loading = true
              case Opcodes.ISTORE | Opcodes.LSTORE | Opcodes.FSTORE | Opcodes.DSTORE | Opcodes.ASTORE => loading = false
              case Opcodes.RET => throw new Exception("Not supported RET");
            }
            if (loading) {
              stack.push(new VarExpr(varn.`var`))
            } else {
              statements.nodes.append(new AssignStm(new VarExpr(varn.`var`), stack.pop()))
            }

          case linen: LineNumberNode =>
            statements.nodes.append(new LineNumberStm(linen.line))


          case typen: TypeInsnNode =>
            typen.getOpcode match {
              case Opcodes.NEW => stack.push(new NewExpr(typen.desc))
              case Opcodes.ANEWARRAY => stack.push(new NewArrayExpr(stack.pop(), typen.desc))
              case Opcodes.CHECKCAST => stack.push(new CheckCastExpr(stack.pop(), typen.desc))
              case Opcodes.INSTANCEOF => stack.push(new InstanceofExpr(stack.pop(), typen.desc))
            }

          case ldc:LdcInsnNode =>
            stack.push(new ConstExpr(ldc.cst))

          case label:LabelNode =>
            statements.nodes.append(new LabelStm(label.getLabel))

          case insn: InsnNode =>
            insn.getOpcode match {
              case Opcodes.RETURN =>
                statements.nodes.append(new ReturnStm(new VoidExpr))
              case Opcodes.LRETURN | Opcodes.IRETURN | Opcodes.ARETURN =>
                statements.nodes.append(new ReturnStm(stack.pop()))
              case
                Opcodes.IADD | Opcodes.ISUB | Opcodes.IMUL | Opcodes.IREM | Opcodes.IDIV
                | Opcodes.LADD | Opcodes.LSUB | Opcodes.LMUL | Opcodes.LREM | Opcodes.LDIV
              =>
                val right = stack.pop()
                val left = stack.pop()
                stack.push(new BinOp(left, right, insn.getOpcode match {
                  case Opcodes.IADD | Opcodes.LADD => "+"
                  case Opcodes.ISUB | Opcodes.LSUB => "-"
                  case Opcodes.IMUL | Opcodes.LMUL => "*"
                  case Opcodes.IREM | Opcodes.LREM => "%"
                  case Opcodes.IDIV | Opcodes.LDIV => "/"
                }))
              case Opcodes.ICONST_M1 => stack.push(new ConstExpr(-1))
              case Opcodes.ICONST_0 => stack.push(new ConstExpr(0))
              case Opcodes.ICONST_1 => stack.push(new ConstExpr(1))
              case Opcodes.ICONST_2 => stack.push(new ConstExpr(2))
              case Opcodes.ICONST_3 => stack.push(new ConstExpr(3))
              case Opcodes.ICONST_4 => stack.push(new ConstExpr(4))
              case Opcodes.ICONST_5 => stack.push(new ConstExpr(5))
              case Opcodes.AALOAD =>
                val index = stack.pop()
                val arrayref = stack.pop()
                stack.push(new ArrayAccessExpr(arrayref, index))
              case Opcodes.AASTORE =>
                val value = stack.pop()
                val index = stack.pop()
                val arrayref = stack.pop()
                statements.nodes.append(new AssignStm(new ArrayAccessExpr(arrayref, index), value))
              case Opcodes.DUP =>
                val v = stack.pop()
                val expr = new TempExpr(tempindex)
                stack.push(expr)
                stack.push(new AssignTemp(tempindex, v))
                tempindex += 1
              case Opcodes.I2L => stack.push(new CastExpr(stack.pop(), "s32", "s64"))
              case Opcodes.I2F => stack.push(new CastExpr(stack.pop(), "s32", "f32"))
              case Opcodes.I2D => stack.push(new CastExpr(stack.pop(), "s32", "f64"))

              case Opcodes.L2I => stack.push(new CastExpr(stack.pop(), "s64", "s32"))
              case Opcodes.L2F => stack.push(new CastExpr(stack.pop(), "s64", "f32"))
              case Opcodes.L2D => stack.push(new CastExpr(stack.pop(), "s64", "f64"))

              case Opcodes.F2I => stack.push(new CastExpr(stack.pop(), "f32", "s32"))
              case Opcodes.F2L => stack.push(new CastExpr(stack.pop(), "f32", "s64"))
              case Opcodes.F2D => stack.push(new CastExpr(stack.pop(), "f32", "f64"))

              case Opcodes.D2I => stack.push(new CastExpr(stack.pop(), "f64", "s32"))
              case Opcodes.D2L => stack.push(new CastExpr(stack.pop(), "f64", "s64"))
              case Opcodes.D2F => stack.push(new CastExpr(stack.pop(), "f64", "f32"))

              case Opcodes.I2B => stack.push(new CastExpr(stack.pop(), "s32", "s8"))
              case Opcodes.I2C => stack.push(new CastExpr(stack.pop(), "s32", "u16"))
              case Opcodes.I2S => stack.push(new CastExpr(stack.pop(), "s32", "s16"))

              case _ =>
                println(s"INSN: ${insn.getOpcode}")
            }

          case _ =>
            println(_instruction)
        }
        //println(instruction)
      }
    }

    println(CppGenerator.generateCode(statements))
  }
}

object CppGenerator {
  def generateCode(node: Node): String = {
    node match {
      case expr: Expr =>
        expr match {
          case _return: VoidExpr => ""
          case varexpr: VarExpr => s"var_${varexpr.varIndex}"
          case tempExpr: TempExpr => s"temp_${tempExpr.index}"
          case classref: TypeRefExpr => classref.name
          case arraya:ArrayAccessExpr => generateCode(arraya.expr) + "[" + generateCode(arraya.index) + "]"
          case check:CheckCastExpr => "((" + check.desc + ")(" + generateCode(check.expr) + "))"
          case assignTemp:AssignTemp => "auto temp_" + assignTemp.index + " = " + generateCode(assignTemp.expr)
          case newArrayExpr:NewArrayExpr => "new " + newArrayExpr.desc + "[" + generateCode(newArrayExpr.countExpr) + "]"
          case newExpr:NewExpr => "(new " + newExpr.desc + "())"
          case binop:BinOp => "(" + generateCode(binop.left) + " " + binop.op + " " + generateCode(binop.right) + ")"
          case cast:CastExpr => "((" + cast.to + ")(" + generateCode(cast.expr) + "))"
          case const: ConstExpr =>
            const.value match {
              case string: String => "\"" + const.value.toString + "\""
              case _ => const.value.toString
            }
          case fieldaccess: FieldAccessExpr =>
            fieldaccess.base match {
              case classref: TypeRefExpr =>
                s"${classref.name}::${fieldaccess.fieldName}"
              case _ =>
                generateCode(fieldaccess.base) + "->" + fieldaccess.fieldName
            }

          case methodCall: MethodCall =>
            val methodExpr = methodCall.thisExpr match {
              case _:TypeRefExpr => generateCode(methodCall.thisExpr) + "::"
              case _ => generateCode(methodCall.thisExpr) + "->"
            }
            val methodName = methodCall.methodName
            val argsList = (for (arg <- methodCall.args) yield generateCode(arg)).mkString(", ")
            s"${methodExpr}${methodName}(${argsList})"

          case _ => node.toString
        }
      case stm: Stm =>
        stm match {
          case list: StmList => (for (node2 <- list.nodes) yield generateCode(node2)).mkString("")
          case assignStm: AssignStm => generateCode(assignStm.lvalue) + " = " + generateCode(assignStm.expr) + ";\n"
          case exprStm: ExprStm => generateCode(exprStm.expr) + ";\n"
          case _return: ReturnStm => s"return ${generateCode(_return.expr)};\n"
          //case linen: LineNumberStm => s"// line ${linen.line}\n"
          case linen: LineNumberStm => ""
          case _ => node.toString + "\n"
        }
      case _ => node.toString + "\n"
    }
  }
}

trait Node

class Expr extends Node
class VoidExpr extends Expr
class LValue extends Expr
class VarExpr(val varIndex: Int) extends LValue
class TempExpr(val index: Int) extends LValue
class FieldAccessExpr(val base: Expr, val fieldName: String, val fieldDesc: String = "") extends LValue
class TypeRefExpr(val name: String) extends Expr
class MethodCall(val className: String, val methodName: String, val methodType: String, val thisExpr: Expr, val args: Array[Expr]) extends Expr
class NewExpr(val desc:String) extends Expr
class NewArrayExpr(val countExpr:Expr, val desc:String) extends Expr
class CheckCastExpr(val expr:Expr, val desc:String) extends Expr
class InstanceofExpr(val expr:Expr, val desc:String) extends Expr
class ConstExpr(val value:Any) extends Expr
class ArrayAccessExpr(val expr:Expr, val index:Expr) extends LValue
class AssignTemp(val index:Int, val expr:Expr) extends Expr
class BinOp(val left:Expr, val right:Expr, val op:String) extends Expr
class CastExpr(val expr: Expr, val from: String, val to: String) extends Expr

class Stm extends Node
class ReturnStm(val expr: Expr) extends Stm
class ExprStm(val expr: Expr) extends Stm
class AssignStm(val lvalue: LValue, val expr: Expr) extends Stm
class StmList(val nodes: ListBuffer[Stm] = new ListBuffer[Stm]()) extends Stm
class LineNumberStm(val line:Int) extends Stm
class LabelStm(val id:Label) extends Stm
