package old

import java.io.{File, InputStream}
import java.util.jar.JarFile

import org.objectweb.asm.{ClassReader, Opcodes, Label, Type}
import org.objectweb.asm.tree._

import scala.collection.{JavaConverters, mutable}
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class old {
  def handleClass(is: InputStream) = {
    val cr = new ClassReader(is)
    val cn = new ClassNode(Opcodes.ASM5)
    cr.accept(cn, 0)
    val methods = new ListBuffer[MethodResult]
    for (_method <- cn.methods.asScala) {
      val method = _method.asInstanceOf[MethodNode]
      methods.append(new MethodGenerator().handle(cn, method))
    }
    println(CppGenerator.generatePrefix)
    println("class " + cn.name + "{\n")
    for (method <- methods) println(method.signature)
    println("};\n")
    for (method <- methods) println(method.body)
  }
}

class MethodGenerator {
  val statements = StmList()
  var argumentsAllocated = new ListBuffer[Var]
  var variablesAllocated = new ListBuffer[Var]
  var variables = new Array[Var](0)
  val stack = new mutable.Stack[Expr]()
  var varIndex = 0

  private def getVarIndex() = {
    varIndex += 1
    varIndex
  }

  private def variablesFromMethod(method: MethodNode): Unit = {
    val items = new ListBuffer[Var]()
    val nodes = method.instructions.toArray
    for (localIndex <- 0 to method.localVariables.size - 1) {
      val lvar = method.localVariables.get(localIndex).asInstanceOf[LocalVariableNode]
      val kind = Type.getType(lvar.desc)
      val isArgument = (lvar.start.getPrevious == null)
      val variable = new Var(kind, lvar.name, lvar.index, nodes.indexOf(lvar.start) - 1, nodes.indexOf(lvar.end), isArgument)
      if (isArgument) {
        argumentsAllocated.append(variable)
      } else {
        variablesAllocated.append(variable)
      }
      items.append(variable)
      kind.getSort match {
        case Type.LONG | Type.DOUBLE =>
          items.append(variable)
        case _ =>
      }
    }
    variables = items.toArray
  }

  private def getVariable(method: MethodNode, node:InfoNode, varIndex:Int):Var = {
    for (variable <- variables) {
      //println(variable.index + ";" + variable.start + ";" + variable.end)
      if ((varIndex == variable.index) && (node.index >= variable.start) && (node.index <= variable.end)) {
        return variable
      }
    }
    throw new Exception(s"Can't find variable with 'var($varIndex)@offset(${node.index}):'")
  }

  class InfoNode(val index:Int, val node:AbstractInsnNode) {
    var variables:Array[Var] = null
  }

  def createInfoNode(method: MethodNode) = {
    val nodes = new ListBuffer[InfoNode]
    var index = 0
    val labels = new mutable.HashMap[Label, Int]
    for (_instruction <- method.instructions.iterator().asScala) {
      nodes.append(new InfoNode(index, _instruction.asInstanceOf[AbstractInsnNode]))
      index += 1
    }
    nodes.toArray
  }

  def handle(classNode:ClassNode, method: MethodNode) = {
    var tempIndex = 0

    variablesFromMethod(method)

    if (method.instructions.size > 0) {
      for (node <- createInfoNode(method).drop(1).dropRight(1)) { // remove start and end labels
        //println(_instruction)

        val _instruction = node.node

        //val instruction = _instruction.asInstanceOf[AbstractInsnNode]
        _instruction match {
          case field: FieldInsnNode =>
            field.getOpcode match {
              case Opcodes.GETSTATIC =>
                //throw new Exception("Not implemented GETSTATIC")
                stack.push(FieldAccessExpr(TypeRefExpr(field.owner), field.name))

              case Opcodes.PUTSTATIC =>
                val value = stack.pop()
                statements.nodes.append(AssignStm(FieldAccessExpr(TypeRefExpr(field.owner), field.name), value))

              case Opcodes.GETFIELD =>
                val ref = stack.pop()
                stack.push(FieldAccessExpr(ref, field.name))

              case Opcodes.PUTFIELD =>
                val ref = stack.pop()
                val value = stack.pop()
                statements.nodes.append(AssignStm(FieldAccessExpr(ref, field.name), value))
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
              TypeRefExpr(method.owner)
            }
            stack.push(MethodCallExpr(method.owner, method.name, method.desc, thisExpr, argumentExprs.toArray))
            if (methodType.getReturnType.getSort == Type.VOID) {
              statements.nodes.append(ExprStm(stack.pop()))
            }

          //methodType.getReturnType
          //method.desc
          //method.itf
          //println(s"CALL: ${method.owner}.${method.name} :: ${method.desc}")

          case frame: FrameNode =>
          /*
          println("frame!! : " + frame.`type`)

          frame.`type` match {
            case Opcodes.F_NEW =>
            case Opcodes.F_FULL =>
            case Opcodes.F_APPEND =>
            case Opcodes.F_CHOP =>
            case Opcodes.F_SAME =>
            case Opcodes.F_SAME1 =>
          }
          */

          /*
          println(frame.`type`)
          println(frame.local)
          println(frame.stack)
          */

          case iinc:IincInsnNode =>
            val variable = getVariable(method, node, iinc.`var`)
            statements.nodes.append(AssignStm(VarExpr(variable), BinOp(VarExpr(variable), ConstExpr(1), "+")))

          case varn: VarInsnNode =>
            var loading = false
            varn.getOpcode match {
              case Opcodes.ILOAD | Opcodes.LLOAD | Opcodes.FLOAD | Opcodes.DLOAD | Opcodes.ALOAD => loading = true
              case Opcodes.ISTORE | Opcodes.LSTORE | Opcodes.FSTORE | Opcodes.DSTORE | Opcodes.ASTORE => loading = false
              case Opcodes.RET => throw new Exception("Not supported RET and deprecated in java6");
            }
            val variable = getVariable(method, node, varn.`var`)
            if (loading) {
              stack.push(VarExpr(variable))
            } else {
              statements.nodes.append(AssignStm(VarExpr(variable), stack.pop()))
            }

          case linen: LineNumberNode =>
            statements.nodes.append(LineNumberStm(linen.line))


          case typen: TypeInsnNode =>
            val kind = Type.getType(typen.desc)
            typen.getOpcode match {
              case Opcodes.NEW => stack.push(NewExpr(kind))
              case Opcodes.ANEWARRAY => stack.push(NewArrayExpr(stack.pop(), kind))
              case Opcodes.CHECKCAST => stack.push(CheckCastExpr(stack.pop(), kind))
              case Opcodes.INSTANCEOF => stack.push(InstanceofExpr(stack.pop(), kind))
            }

          case ldc:LdcInsnNode =>
            stack.push(ConstExpr(ldc.cst))

          case label:LabelNode =>
            statements.nodes.append(LabelStm(label.getLabel))

          case jump:JumpInsnNode =>
            statements.nodes.append(jump.getOpcode match {
              case Opcodes.IFEQ | Opcodes.IFNE | Opcodes.IFLT | Opcodes.IFGE | Opcodes.IFGT | Opcodes.IFLE =>
                val left = stack.pop()
                val right = ConstExpr(0)
                val op = jump.getOpcode match {
                  case Opcodes.IFEQ => "=="
                  case Opcodes.IFNE => "!="
                  case Opcodes.IFLT => "<"
                  case Opcodes.IFGE => ">="
                  case Opcodes.IFGT => ">"
                  case Opcodes.IFLE => "<="
                }
                JumpIfStm(BinOp(left, right, op), jump.label.getLabel)

              case
                Opcodes.IF_ICMPEQ | Opcodes.IF_ICMPNE | Opcodes.IF_ICMPLT | Opcodes.IF_ICMPGE | Opcodes.IF_ICMPGT
                | Opcodes.IF_ICMPLE | Opcodes.IF_ACMPEQ | Opcodes.IF_ACMPNE
              =>
                val right = stack.pop()
                val left = stack.pop()
                val op = jump.getOpcode match {
                  case Opcodes.IF_ICMPEQ => "=="
                  case Opcodes.IF_ICMPNE => "!="
                  case Opcodes.IF_ICMPLT => "<"
                  case Opcodes.IF_ICMPGE => ">="
                  case Opcodes.IF_ICMPGT => ">"
                  case Opcodes.IF_ICMPLE => "<="
                  case Opcodes.IF_ACMPEQ => "=="
                  case Opcodes.IF_ACMPNE => "!="
                }
                JumpIfStm(BinOp(left, right, op), jump.label.getLabel)

              case Opcodes.GOTO => JumpStm(jump.label.getLabel)
              case Opcodes.IFNULL => JumpIfStm(BinOp(stack.pop(), ConstExpr(null), "=="), jump.label.getLabel)
              case Opcodes.IFNONNULL => JumpIfStm(BinOp(stack.pop(), ConstExpr(null), "!="), jump.label.getLabel)

              case Opcodes.JSR => throw new Exception("Not supported JSR and deprecated in java6");
            })

          case insn: InsnNode =>
            insn.getOpcode match {
              case Opcodes.RETURN =>
                statements.nodes.append(ReturnStm(VoidExpr()))
              case Opcodes.LRETURN | Opcodes.IRETURN | Opcodes.ARETURN =>
                statements.nodes.append(ReturnStm(stack.pop()))
              case
                Opcodes.IADD | Opcodes.ISUB | Opcodes.IMUL | Opcodes.IREM | Opcodes.IDIV
                | Opcodes.LADD | Opcodes.LSUB | Opcodes.LMUL | Opcodes.LREM | Opcodes.LDIV
                | Opcodes.FADD | Opcodes.FSUB | Opcodes.FMUL | Opcodes.FREM | Opcodes.FDIV
                | Opcodes.DADD | Opcodes.DSUB | Opcodes.DMUL | Opcodes.DREM | Opcodes.DDIV
              =>
                val right = stack.pop()
                val left = stack.pop()
                stack.push(BinOp(left, right, insn.getOpcode match {
                  case Opcodes.IADD | Opcodes.LADD | Opcodes.FADD | Opcodes.DADD => "+"
                  case Opcodes.ISUB | Opcodes.LSUB | Opcodes.FSUB | Opcodes.DSUB => "-"
                  case Opcodes.IMUL | Opcodes.LMUL | Opcodes.FMUL | Opcodes.DMUL => "*"
                  case Opcodes.IREM | Opcodes.LREM | Opcodes.FREM | Opcodes.DREM => "%"
                  case Opcodes.IDIV | Opcodes.LDIV | Opcodes.FDIV | Opcodes.DDIV => "/"
                }))
              case Opcodes.ICONST_M1 => stack.push(ConstExpr(-1))
              case Opcodes.ICONST_0 => stack.push(ConstExpr(0))
              case Opcodes.ICONST_1 => stack.push(ConstExpr(1))
              case Opcodes.ICONST_2 => stack.push(ConstExpr(2))
              case Opcodes.ICONST_3 => stack.push(ConstExpr(3))
              case Opcodes.ICONST_4 => stack.push(ConstExpr(4))
              case Opcodes.ICONST_5 => stack.push(ConstExpr(5))
              case Opcodes.AALOAD =>
                val index = stack.pop()
                val arrayref = stack.pop()
                stack.push(ArrayAccessExpr(arrayref, index))
              case Opcodes.AASTORE =>
                val value = stack.pop()
                val index = stack.pop()
                val arrayref = stack.pop()
                statements.nodes.append(AssignStm(ArrayAccessExpr(arrayref, index), value))
              case Opcodes.DUP =>
                val v = stack.pop()
                val expr = TempExpr(tempIndex)
                stack.push(expr)
                stack.push(AssignTemp(tempIndex, v))
                tempIndex += 1
              case Opcodes.I2L => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.LONG_TYPE))
              case Opcodes.I2F => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.FLOAT_TYPE))
              case Opcodes.I2D => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.DOUBLE_TYPE))

              case Opcodes.L2I => stack.push(CastExpr(stack.pop(), Type.LONG_TYPE, Type.INT_TYPE))
              case Opcodes.L2F => stack.push(CastExpr(stack.pop(), Type.LONG_TYPE, Type.FLOAT_TYPE))
              case Opcodes.L2D => stack.push(CastExpr(stack.pop(), Type.LONG_TYPE, Type.DOUBLE_TYPE))

              case Opcodes.F2I => stack.push(CastExpr(stack.pop(), Type.FLOAT_TYPE, Type.INT_TYPE))
              case Opcodes.F2L => stack.push(CastExpr(stack.pop(), Type.FLOAT_TYPE, Type.LONG_TYPE))
              case Opcodes.F2D => stack.push(CastExpr(stack.pop(), Type.FLOAT_TYPE, Type.DOUBLE_TYPE))

              case Opcodes.D2I => stack.push(CastExpr(stack.pop(), Type.DOUBLE_TYPE, Type.INT_TYPE))
              case Opcodes.D2L => stack.push(CastExpr(stack.pop(), Type.DOUBLE_TYPE, Type.LONG_TYPE))
              case Opcodes.D2F => stack.push(CastExpr(stack.pop(), Type.DOUBLE_TYPE, Type.FLOAT_TYPE))

              case Opcodes.I2B => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.BYTE_TYPE))
              case Opcodes.I2C => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.CHAR_TYPE))
              case Opcodes.I2S => stack.push(CastExpr(stack.pop(), Type.INT_TYPE, Type.SHORT_TYPE))

              case _ =>
                throw new Exception(s"Unhandled INSN ${insn.getOpcode}");
            }

          case _ =>
            throw new Exception(s"Unhandled instruction ${_instruction}");
        }
        //println(instruction)
      }
    }

    new MethodResult(
      CppGenerator.generateMethodSignature(classNode.name, method, statements, variablesAllocated, argumentsAllocated),
      CppGenerator.generateMethod(classNode.name, method, statements, variablesAllocated, argumentsAllocated)
    )
  }
}

class MethodResult(val signature:String, val body:String)

trait Vfs {
  def exists(path:String): Boolean
  def read(path:String): InputStream
}

class JarVfs(file:File) extends Vfs {
  private val jar = new JarFile(file)

  override def exists(path: String): Boolean = {
    jar.getEntry(path) != null
  }

  override def read(path: String): InputStream = {
    jar.getInputStream(jar.getEntry(path))
  }
}
