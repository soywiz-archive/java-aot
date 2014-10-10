package ast

import org.objectweb.asm.{Type, Opcodes}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ProcessAst {
  val stack = new mutable.Stack[Expr]
  val locals = new ListBuffer[Local]
  val stms = new ListBuffer[Stm]

  def process(node:MethodNode):Unit = {
    process(node.instructions)
  }

  def process(list:InsnList): Unit = {
    for (item <- list.toArray) {
      processAbstract(item)
    }
  }

  def process(i: JumpInsnNode): Unit = {
    lazy val op = i.getOpcode match {
      case IFEQ | IF_ICMPEQ | IF_ACMPEQ | IFNULL => "=="
      case IFNE | IF_ICMPNE | IF_ACMPNE | IFNONNULL => "!="
      case IFLT | IF_ICMPLT => "<"
      case IFGT | IF_ICMPGT => ">"
      case IFGE | IF_ICMPGE => ">="
      case IFLE | IF_ICMPLE => "<="
    }

    i.getOpcode match {
      case IFEQ | IFNE | IFLT | IFGT | IFGE | IFLE =>
        stms.append(GotoStm(Binop(op, (stack.pop(), IntConstant(0))), i.label))

      case IF_ICMPEQ |IF_ICMPNE |IF_ICMPLT |IF_ICMPGT |IF_ICMPGE |IF_ICMPLE | IF_ACMPEQ | IF_ACMPNE =>
        stms.append(GotoStm(Binop(op, stackPop2()), i.label))

      case IFNULL | IFNONNULL => stms.append(GotoStm(Binop(op, (stack.pop(), NullConstant())), i.label))
      case GOTO => stms.append(GotoStm(BoolConstant(true), i.label))
      case JSR => throw new Exception("Not supported JSR")
    }
  }

  def process(i: MethodInsnNode): Unit = {
    i match {
      case INVOKEVIRTUAL => throw new NotImplementedError()
      case INVOKESPECIAL => throw new NotImplementedError()
      case INVOKESTATIC => throw new NotImplementedError()
      case INVOKEINTERFACE => throw new NotImplementedError()
    }
  }

  def process(i: TypeInsnNode): Unit = {
    i match {
      case NEW => stack.push(New(ClassType(i.desc)))
      case ANEWARRAY => stack.push(NewArray(ClassType(i.desc), stack.pop()))
      case CHECKCAST => stack.push(CheckCast(ClassType(i.desc), stack.pop()))
      case INSTANCEOF => stack.push(InstanceOf(ClassType(i.desc), stack.pop()))
    }
  }

  def process(i: VarInsnNode): Unit = {
    i match {
      case ILOAD | LLOAD | FLOAD | DLOAD | ALOAD => stack.push(getLocal(i.`var`))
      case ISTORE | LSTORE | FSTORE | DSTORE | ASTORE | RET => stms.append(Assign(getLocal(i.`var`), stack.pop()))
    }
  }

  def processAbstract(i: AbstractInsnNode): Unit = {
    i match {
      case i:FieldInsnNode => process(i)
      //case i:IincInsnNode => process(i)
      //case i:FrameNode => process(i)
      case i:InsnNode => process(i)
      //case i: InvokeDynamicInsnNode => process(i)
      case i: JumpInsnNode => process(i)
      //case i: LabelNode => process(i)
      case i: LdcInsnNode => process(i)
      //case i: LineNumberNode => process(i)
      //case i: LookupSwitchInsnNode => process(i)
      case i: MethodInsnNode => process(i)
      //case i: MultiANewArrayInsnNode => process(i)
      //case i: TableSwitchInsnNode => process(i)
      case i : TypeInsnNode => process(i)
      case i: VarInsnNode => process(i)
    }
  }

  def process(i:LdcInsnNode):Unit = {
    i.cst match {
      case v:Int => stack.push(IntConstant(v))
      case v:Long => stack.push(LongConstant(v))
      case v:Double => stack.push(DoubleConstant(v))
      case v:String => stack.push(StringConstant(v))
      //case v:Type => stack.push(ClassConstant(v))
    }
  }

  def process(i:FieldInsnNode):Unit = {
    val fieldRef = FieldRef(i.owner, i.name, NodeUtils.typeFromDesc(i.desc))
    i.getOpcode match {
      case Opcodes.GETSTATIC => stack.push(Field(fieldRef))
      case Opcodes.GETFIELD => stack.push(Field(fieldRef, stack.pop()))
      case Opcodes.PUTSTATIC => stms.append(Assign(Field(fieldRef), stack.pop()))
      case Opcodes.PUTFIELD => stms.append(Assign(Field(fieldRef, stack.pop()), stack.pop()))
    }
  }

  def stackPop2() = { val r = stack.pop(); val l = stack.pop(); (l, r) }
  def stackPop3() = { val r = stack.pop(); val m = stack.pop(); val l = stack.pop(); (l, m, r) }

  def getLocal(index:Int) = {
    Local(index)
  }

  def allocLocal(index:Int = -1) = {
    val local = Local(index)
    locals.append(local)
    local
  }

  def process(i:InsnNode): Unit = {
    i.getOpcode match {
      case NOP =>
      case ACONST_NULL => stack.push(NullConstant())
      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 =>
        stack.push(IntConstant(i.getOpcode - ICONST_0))

      case LCONST_0 | LCONST_1 => stack.push(LongConstant(i.getOpcode - LCONST_0))
      case FCONST_0 | FCONST_1 | FCONST_2 => stack.push(FloatConstant(i.getOpcode - FCONST_0))
      case DCONST_0 | DCONST_1 => stack.push(DoubleConstant(i.getOpcode - DCONST_0))

      case IALOAD | LALOAD | FALOAD | DALOAD | AALOAD | BALOAD | CALOAD | SALOAD =>
        stack.push(ArrayAccess(stackPop2()))

      case IASTORE | LASTORE | FASTORE |DASTORE |AASTORE |BASTORE |CASTORE |SASTORE =>
        val v = stack.pop()
        val aa = stackPop2()
        stms.append(Assign(ArrayAccess(aa), v))

      case POP => stack.pop()
      case POP2 => stackPop2()

      case DUP =>
        val value = stack.pop()
        val local = allocLocal(value.getType)
        stms.append(Assign(local, value))
        stack.push(local)

      case DUP_X1 => throw new NotImplementedError()
      case DUP_X2 => throw new NotImplementedError()
      case DUP2 => throw new NotImplementedError()
      case DUP2_X1 => throw new NotImplementedError()
      case DUP2_X2 => throw new NotImplementedError()

      case SWAP => val a = stack.pop(); val b = stack.pop(); stack.push(a); stack.push(b)

      case IADD | LADD | FADD | DADD => stack.push(Binop("+", stackPop2()))
      case ISUB | LSUB | FSUB | DSUB => stack.push(Binop("-", stackPop2()))
      case IMUL | LMUL | FMUL | DMUL => stack.push(Binop("*", stackPop2()))
      case IDIV | LDIV | FDIV | DDIV => stack.push(Binop("/", stackPop2()))
      case IREM | LREM | FREM | DREM => stack.push(Binop("%", stackPop2()))
      case INEG | LNEG | FNEG | DNEG => stack.push(Unop("-", stack.pop()))

      case ISHL | LSHL => stack.push(Binop("<<", stackPop2()))
      case ISHR | LSHR => stack.push(Binop(">>", stackPop2()))
      case IUSHR | LUSHR => stack.push(Binop(">>>", stackPop2()))

      case IAND | LAND => stack.push(Binop("&", stackPop2()))
      case IOR | LOR => stack.push(Binop("|", stackPop2()))
      case IXOR | LXOR => stack.push(Binop("~", stackPop2()))

      case I2L | F2L | D2L => stack.push(Conv(stack.pop(), classOf[Long]))
      case I2F | L2F | D2F => stack.push(Conv(stack.pop(), classOf[Float]))
      case I2D | L2D | F2D => stack.push(Conv(stack.pop(), classOf[Double]))
      case L2I | F2I | D2I => stack.push(Conv(stack.pop(), classOf[Int]))
      case I2B => stack.push(Conv(stack.pop(), classOf[Byte]))
      case I2C => stack.push(Conv(stack.pop(), classOf[Char]))
      case I2S => stack.push(Conv(stack.pop(), classOf[Short]))

      case LCMP => stack.push(Binop("cmp", stackPop2()))
      case FCMPL | DCMPL => stack.push(Binop("cmpl", stackPop2()))
      case FCMPG | DCMPG => stack.push(Binop("cmpg", stackPop2()))
      case IRETURN | LRETURN | FRETURN | DRETURN | ARETURN => stms.append(ReturnStm(stack.pop()))
      case RETURN => stms.append(ReturnStm())

      case ARRAYLENGTH => stack.push(ArrayLength(stack.pop()))

      case ATHROW => stms.append(ThrowStm(stack.pop()))
      case MONITORENTER => stms.append(MonitorEnter(stack.pop()))
      case MONITOREXIT => stms.append(MonitorExit(stack.pop()))
    }
  }
}
