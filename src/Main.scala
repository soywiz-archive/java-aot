import java.io.{File, FileInputStream}

import org.objectweb.asm.tree._
import org.objectweb.asm.{Label, ClassReader, Opcodes, Type}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Main extends App {
  //handleClass(new File("target/classes/Main.class"))
  handleClass(new File("target/classes/CppGenerator$.class"))

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
                stack.push(new FieldAccessExpr(new ClassReference(field.owner), field.name))

              case Opcodes.PUTSTATIC =>
                val value = stack.pop()
                statements.nodes.append(new AssignStm(new FieldAccessExpr(new ClassReference(field.owner), field.name), value))

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
              case Opcodes.INVOKESPECIAL => isStatic = true
              case Opcodes.INVOKESTATIC => isStatic = true
              case Opcodes.INVOKEINTERFACE => isStatic = false
            }
            val methodType = Type.getMethodType(method.desc)
            val argumentTypes = methodType.getArgumentTypes
            var thisExpr: Expr = new VoidExpr()
            val argumentExprs = (for (n <- argumentTypes) yield stack.pop()).reverse.toArray
            if (!isStatic) thisExpr = stack.pop()
            stack.push(new MethodCall(method.owner, method.name, method.desc, thisExpr, argumentExprs.toArray))
            if (methodType.getReturnType.getSort == Type.VOID) {
              statements.nodes.append(new ExprStm(stack.pop()))
            }

          //methodType.getReturnType
          //method.desc
          //method.itf
          //println(s"CALL: ${method.owner}.${method.name} :: ${method.desc}")

          case varn: VarInsnNode =>
            stack.push(new VarExpr(varn.`var`))
          //println(s"var_${varn.`var`}")

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
                var expr = new TempExpr(tempindex)
                stack.push(new AssignTemp(tempindex, v))
                tempindex += 1
                //stack.push(v)
                //stack.push(v)
                stack.push(expr)
              case _ =>
                println(s"INSN: ${insn.getOpcode}")
            }

          case _ =>
            println(_instruction)
        }
        //println(instruction)
      }
    }

    println(CppGenerator.generate(statements))
  }
}

object CppGenerator {
  def generate(node: Node): String = {
    node match {
      case expr: Expr =>
        expr match {
          case _return: VoidExpr => ""
          case varexpr: VarExpr => s"var_${varexpr.varIndex}"
          case tempExpr: TempExpr => s"temp_${tempExpr.index}"
          case classref: ClassReference => classref.name
          case arraya:ArrayAccessExpr => generate(arraya.expr) + "[" + generate(arraya.index) + "]"
          case check:CheckCastExpr => "((" + check.desc + ")(" + generate(check.expr) + "))"
          case assignTemp:AssignTemp => "auto temp_" + assignTemp.index + " = " + generate(assignTemp.expr)
          case newArrayExpr:NewArrayExpr => "new " + newArrayExpr.desc + "[" + generate(newArrayExpr.countExpr) + "]"
          case const: ConstExpr =>
            const.value match {
              case string: String => "\"" + const.value.toString + "\""
              case _ => const.value.toString
            }
          case fieldaccess: FieldAccessExpr =>
            fieldaccess.base match {
              case classref: ClassReference =>
                s"${classref.name}::${fieldaccess.fieldName}"
              case _ =>
                generate(fieldaccess.base) + "->" + fieldaccess.fieldName
            }

          case methodCall: MethodCall =>
            val methodExpr = generate(methodCall.thisExpr)
            val methodName = methodCall.methodName
            val argsList = (for (arg <- methodCall.args) yield generate(arg)).mkString(", ")
            s"${methodExpr}->${methodName}(${argsList})"

          case _ => node.toString
        }
      case stm: Stm =>
        stm match {
          case list: StmList => (for (node2 <- list.nodes) yield generate(node2)).mkString("")
          case assignStm: AssignStm => generate(assignStm.lvalue) + " = " + generate(assignStm.expr) + ";\n"
          case exprStm: ExprStm => generate(exprStm.expr) + ";\n"
          case _return: ReturnStm => s"return ${generate(_return.expr)};\n"
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
class ClassReference(val name: String) extends Expr
class MethodCall(val className: String, val methodName: String, val methodType: String, val thisExpr: Expr, val args: Array[Expr]) extends Expr
class NewExpr(val desc:String) extends Expr
class NewArrayExpr(val countExpr:Expr, val desc:String) extends Expr
class CheckCastExpr(val expr:Expr, val desc:String) extends Expr
class InstanceofExpr(val expr:Expr, val desc:String) extends Expr
class ConstExpr(val value:Any) extends Expr
class ArrayAccessExpr(val expr:Expr, val index:Expr) extends LValue
class AssignTemp(val index:Int, val expr:Expr) extends Expr

class Stm extends Node
class ReturnStm(val expr: Expr) extends Stm
class ExprStm(val expr: Expr) extends Stm
class AssignStm(val lvalue: LValue, val expr: Expr) extends Stm
class StmList(val nodes: ListBuffer[Stm] = new ListBuffer[Stm]()) extends Stm
class LineNumberStm(val line:Int) extends Stm
class LabelStm(val id:Label) extends Stm
