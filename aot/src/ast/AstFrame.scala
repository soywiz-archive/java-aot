package ast

import org.objectweb.asm.{Type, Opcodes}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AstFrame(context:AstMethodContext, initialLocals:Array[LValue] = null, initialStack:List[Expr] = null) {
  val stack = new mutable.Stack[Expr]
  val stms = new ListBuffer[Stm]

  initialStack.foreach(stack.push)

  def process(list:Seq[AbstractInsnNode]): Unit = {
    for (item <- list.toArray) {
      processAbstract(item)
    }
    if (context.debug) {
      stms.foreach(println)
      println(stack)
    }
  }

  private def process(i: JumpInsnNode): Unit = {
    lazy val op = i.getOpcode match {
      case IFEQ | IF_ICMPEQ | IF_ACMPEQ | IFNULL => "=="
      case IFNE | IF_ICMPNE | IF_ACMPNE | IFNONNULL => "!="
      case IFLT | IF_ICMPLT => "<"
      case IFGT | IF_ICMPGT => ">"
      case IFGE | IF_ICMPGE => ">="
      case IFLE | IF_ICMPLE => "<="
    }

    val labelRef = context.getLabelRef(i.label)

    i.getOpcode match {
      case IFEQ | IFNE | IFLT | IFGT | IFGE | IFLE =>
        stms.append(BranchStm(Binop(op, (stackPop(), IntConstant(0))), labelRef))

      case IF_ICMPEQ |IF_ICMPNE |IF_ICMPLT |IF_ICMPGT |IF_ICMPGE |IF_ICMPLE | IF_ACMPEQ | IF_ACMPNE =>
        stms.append(BranchStm(Binop(op, stackPop2()), labelRef))

      case IFNULL | IFNONNULL => stms.append(BranchStm(Binop(op, (stackPop(), NullConstant())), labelRef))
      case GOTO => stms.append(JumpStm(labelRef))
      case JSR => throw new Exception("Not supported JSR")
    }
  }

  private def process(i: MethodInsnNode): Unit = {
    val clazzRef = ClassType(i.owner)
    val methodType = NodeUtils.typeFromDesc(i.desc).asInstanceOf[MethodType]
    val methodRef = MethodRef(clazzRef, methodType, i.name)

    val parameters = stackPopN(methodType.arguments.length).toList
    val argsThis = i.getOpcode match {
      case INVOKEINTERFACE | INVOKESPECIAL | INVOKEVIRTUAL => List(stackPop())
      case INVOKESTATIC => List()
    }
    val args = argsThis ::: parameters
    val expr = Invoke(methodRef, args)

    if (methodType.hasReturnValue) {
      stackPush(expr)
    } else {
      stms.append(ExprStm(expr))
    }
  }

  private def process(i: TypeInsnNode): Unit = {
    i.getOpcode match {
      case NEW => stackPush(New(ClassType(i.desc)))
      case ANEWARRAY => stackPush(NewArray(ClassType(i.desc), stackPop()))
      case CHECKCAST => stackPush(CheckCast(ClassType(i.desc), stackPop()))
      case INSTANCEOF => stackPush(InstanceOf(ClassType(i.desc), stackPop()))
    }
  }

  private def process(i: VarInsnNode): Unit = {
    i.getOpcode match {
      case ILOAD | LLOAD | FLOAD | DLOAD | ALOAD => stackPush(context.getLocalIn(i, i.`var`))
      case ISTORE | LSTORE | FSTORE | DSTORE | ASTORE | RET => stms.append(Assign(context.getLocalIn(i, i.`var`), stackPop()))
    }
  }

  private def process(node: LabelNode): Unit = {
    stms.append(LabelStm(context.getLabelRef(node)))
  }

  private def process(i: LineNumberNode): Unit = {

  }

  private def process(i: IntInsnNode): Unit = {
    i.getOpcode match {
      case BIPUSH | SIPUSH => stackPush(IntConstant(i.operand))
      case NEWARRAY =>
        stackPush(i.operand match {
          case Opcodes.T_BOOLEAN => NewArray(BoolType(), stackPop())
          case Opcodes.T_BYTE => NewArray(ByteType(), stackPop())
          case Opcodes.T_CHAR => NewArray(CharType(), stackPop())
          case Opcodes.T_SHORT => NewArray(ShortType(), stackPop())
          case Opcodes.T_INT => NewArray(IntType(), stackPop())
          case Opcodes.T_LONG => NewArray(LongType(), stackPop())
          case Opcodes.T_FLOAT => NewArray(FloatType(), stackPop())
          case Opcodes.T_DOUBLE => NewArray(DoubleType(), stackPop())
      })
    }
  }

  private  def process(node: IincInsnNode): Unit = {
    val local = context.getLocalIn(node, node.`var`)
    stms.append(Assign(local, Binop("+", (local, IntConstant(node.incr)))))
  }

  def process(node: InvokeDynamicInsnNode): Unit = {
    throw new Exception("Not implemented InvokeDynamic")
  }

  def process(node: FrameNode): Unit = {

  }

  private def processAbstract(i: AbstractInsnNode): Unit = {
    if (context.debug) println(i + " : " + i.getOpcode)
    i match {
      case i:FieldInsnNode => process(i)
      case i:IntInsnNode => process(i)
      case i:IincInsnNode => process(i)
      case i:FrameNode => process(i)
      case i:InsnNode => process(i)
      case i: InvokeDynamicInsnNode => process(i)
      case i: JumpInsnNode => process(i)
      case i: LabelNode => process(i)
      case i: LdcInsnNode => process(i)
      case i: LineNumberNode => process(i)
      //case i: LookupSwitchInsnNode => process(i)
      case i: MethodInsnNode => process(i)
      //case i: MultiANewArrayInsnNode => process(i)
      //case i: TableSwitchInsnNode => process(i)
      case i : TypeInsnNode => process(i)
      case i: VarInsnNode => process(i)
    }
  }

  private def process(i:LdcInsnNode):Unit = {
    i.cst match {
      case v:Integer => stackPush(IntConstant(v))
      //case v:Long => stack.push(LongConstant(v))
      //case v:Double => stack.push(DoubleConstant(v))
      case v:String => stackPush(StringConstant(v))
      //case v:Type => stack.push(ClassConstant(v))
    }
  }

  private def process(i:FieldInsnNode):Unit = {
    val fieldRef = FieldRef(ClassType(i.owner), NodeUtils.typeFromDesc(i.desc), i.name)
    i.getOpcode match {
      case GETSTATIC => stackPush(StaticField(fieldRef))
      case GETFIELD => stackPush(Field(fieldRef, stackPop()))
      case PUTSTATIC => stms.append(Assign(StaticField(fieldRef), stackPop()))
      case PUTFIELD =>
        expectStack(2)
        stms.append(Assign(Field(fieldRef, stackPop()), stackPop()))
    }
  }

  private def expectStack(count:Int): Unit = {
    if (stack.length < count) {
      throw new Exception(s"Stack expected $count but has ${stack.length}")
    }
  }

  private def stackPop() = { expectStack(1); stack.pop() }
  private def stackPop2() = { expectStack(2); val r = stack.pop(); val l = stack.pop(); (l, r) }
  private def stackPop3() = { expectStack(3); val r = stack.pop(); val m = stack.pop(); val l = stack.pop(); (l, m, r) }
  private def stackPopN(count:Int):Array[Expr] = {
    expectStack(count)
    val out = new Array[Expr](count)
    for (n <- 0 until count) out(count - n - 1) = stack.pop()
    out
  }
  private def stackPush(v:Expr) = stack.push(v)

  private def process(i:InsnNode): Unit = {
    i.getOpcode match {
      case NOP =>
      case ACONST_NULL => stackPush(NullConstant())
      case ICONST_M1 | ICONST_0 | ICONST_1 | ICONST_2 | ICONST_3 | ICONST_4 | ICONST_5 =>
        stackPush(IntConstant(i.getOpcode - ICONST_0))

      case LCONST_0 | LCONST_1 => stackPush(LongConstant(i.getOpcode - LCONST_0))
      case FCONST_0 | FCONST_1 | FCONST_2 => stackPush(FloatConstant(i.getOpcode - FCONST_0))
      case DCONST_0 | DCONST_1 => stackPush(DoubleConstant(i.getOpcode - DCONST_0))

      case IALOAD | LALOAD | FALOAD | DALOAD | AALOAD | BALOAD | CALOAD | SALOAD =>
        stackPush(ArrayAccess(stackPop2()))

      case IASTORE | LASTORE | FASTORE |DASTORE |AASTORE |BASTORE |CASTORE |SASTORE =>
        val v = stackPop()
        val aa = stackPop2()
        stms.append(Assign(ArrayAccess(aa), v))

      case POP => stackPop()
      case POP2 => stackPop2()

      case DUP =>
        val value = stackPop()
        val local = context.allocLocal(value.getType)
        stms.append(Assign(local, value))
        stackPush(local)
        stackPush(local)

      case DUP_X1 => throw new NotImplementedError()
      case DUP_X2 => throw new NotImplementedError()
      case DUP2 => throw new NotImplementedError()
      case DUP2_X1 => throw new NotImplementedError()
      case DUP2_X2 => throw new NotImplementedError()

      case SWAP => val a = stackPop(); val b = stackPop(); stackPush(a); stackPush(b)

      case IADD | LADD | FADD | DADD => stackPush(Binop("+", stackPop2()))
      case ISUB | LSUB | FSUB | DSUB => stackPush(Binop("-", stackPop2()))
      case IMUL | LMUL | FMUL | DMUL => stackPush(Binop("*", stackPop2()))
      case IDIV | LDIV | FDIV | DDIV => stackPush(Binop("/", stackPop2()))
      case IREM | LREM | FREM | DREM => stackPush(Binop("%", stackPop2()))
      case INEG | LNEG | FNEG | DNEG => stackPush(Unop("-", stackPop()))

      case ISHL | LSHL => stackPush(Binop("<<", stackPop2()))
      case ISHR | LSHR => stackPush(Binop(">>", stackPop2()))
      case IUSHR | LUSHR => stackPush(Binop(">>>", stackPop2()))

      case IAND | LAND => stackPush(Binop("&", stackPop2()))
      case IOR | LOR => stackPush(Binop("|", stackPop2()))
      case IXOR | LXOR => stackPush(Binop("~", stackPop2()))

      case I2L | F2L | D2L => stackPush(Conv(stackPop(), LongType()))
      case I2F | L2F | D2F => stackPush(Conv(stackPop(), FloatType()))
      case I2D | L2D | F2D => stackPush(Conv(stackPop(), DoubleType()))
      case L2I | F2I | D2I => stackPush(Conv(stackPop(), IntType()))
      case I2B => stackPush(Conv(stackPop(), ByteType()))
      case I2C => stackPush(Conv(stackPop(), CharType()))
      case I2S => stackPush(Conv(stackPop(), ShortType()))

      case LCMP => stackPush(Binop("cmp", stackPop2()))
      case FCMPL | DCMPL => stackPush(Binop("cmpl", stackPop2()))
      case FCMPG | DCMPG => stackPush(Binop("cmpg", stackPop2()))
      case IRETURN | LRETURN | FRETURN | DRETURN | ARETURN => stms.append(ReturnStm(stackPop()))
      case RETURN => stms.append(ReturnVoidStm())

      case ARRAYLENGTH => stackPush(ArrayLength(stackPop()))

      case ATHROW => stms.append(ThrowStm(stackPop()))
      case MONITORENTER => stms.append(MonitorEnter(stackPop()))
      case MONITOREXIT => stms.append(MonitorExit(stackPop()))
    }
  }
}
