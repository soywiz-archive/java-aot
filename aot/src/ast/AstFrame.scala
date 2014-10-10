package ast

import org.objectweb.asm.{Type, Opcodes}
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AstFrame(_locals:Array[LValue] = null) {
  private val stack = new mutable.Stack[Expr]
  private val locals = new ListBuffer[Local]
  private val stms = new ListBuffer[Stm]

  def process(list:Seq[AbstractInsnNode]): Unit = {
    println("--------------------------")
    for (item <- list.toArray) {
      processAbstract(item)
    }
    stms.foreach(println)
    println(stack)
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

  private def process(i: MethodInsnNode): Unit = {
    val clazzRef = ClassType(i.owner)
    println(i.desc)
    val methodType = NodeUtils.typeFromDesc(i.desc).asInstanceOf[MethodType]
    val methodRef = MethodRef(clazzRef, methodType, i.name)

    val parameters = stackPopN(methodType.arguments.length).toList
    val argsThis = i.getOpcode match {
      case INVOKEINTERFACE | INVOKESPECIAL | INVOKEVIRTUAL => List(stack.pop())
      case INVOKESTATIC => List()
    }
    val args = argsThis ::: parameters
    val expr = Invoke(methodRef, args)

    if (methodType.hasReturnValue) {
      stack.push(expr)
    } else {
      stms.append(ExprStm(expr))
    }
  }

  private def process(i: TypeInsnNode): Unit = {
    i.getOpcode match {
      case NEW => stack.push(New(ClassType(i.desc)))
      case ANEWARRAY => stack.push(NewArray(ClassType(i.desc), stack.pop()))
      case CHECKCAST => stack.push(CheckCast(ClassType(i.desc), stack.pop()))
      case INSTANCEOF => stack.push(InstanceOf(ClassType(i.desc), stack.pop()))
    }
  }

  private def process(i: VarInsnNode): Unit = {
    i.getOpcode match {
      case ILOAD | LLOAD | FLOAD | DLOAD | ALOAD => stack.push(getLocal(i.`var`))
      case ISTORE | LSTORE | FSTORE | DSTORE | ASTORE | RET => stms.append(Assign(getLocal(i.`var`), stack.pop()))
    }
  }

  val labels = new mutable.HashSet[LabelRef]()

  def process(node: LabelNode): Unit = {
    labels.add(LabelRef())
  }

  def process(node: LineNumberNode): Unit = {

  }

  private def processAbstract(i: AbstractInsnNode): Unit = {
    i match {
      case i:FieldInsnNode => process(i)
      //case i:IincInsnNode => process(i)
      //case i:FrameNode => process(i)
      case i:InsnNode => process(i)
      //case i: InvokeDynamicInsnNode => process(i)
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
      case v:Integer => stack.push(IntConstant(v))
      //case v:Long => stack.push(LongConstant(v))
      //case v:Double => stack.push(DoubleConstant(v))
      case v:String => stack.push(StringConstant(v))
      //case v:Type => stack.push(ClassConstant(v))
    }
  }

  private def process(i:FieldInsnNode):Unit = {
    val fieldRef = FieldRef(ClassType(i.owner), NodeUtils.typeFromDesc(i.desc), i.name)
    i.getOpcode match {
      case GETSTATIC => stack.push(StaticField(fieldRef))
      case GETFIELD => stack.push(Field(fieldRef, stack.pop()))
      case PUTSTATIC => stms.append(Assign(StaticField(fieldRef), stack.pop()))
      case PUTFIELD => stms.append(Assign(Field(fieldRef, stack.pop()), stack.pop()))
    }
  }

  private def stackPop2() = { val r = stack.pop(); val l = stack.pop(); (l, r) }
  private def stackPopN(count:Int):Array[Expr] = {
    val out = new Array[Expr](count)
    for (n <- 0 until count) out(count - n - 1) = stack.pop()
    out
  }
  private def stackPop3() = { val r = stack.pop(); val m = stack.pop(); val l = stack.pop(); (l, m, r) }

  private def getLocal(index:Int) = {
    if (index >= _locals.length) {
      throw new Exception
    }
    _locals(index)
  }

  private def allocLocal(kind:NodeType) = {
    val local = Local(kind)
    locals.append(local)
    local
  }

  private def process(i:InsnNode): Unit = {
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

      case I2L | F2L | D2L => stack.push(Conv(stack.pop(), LongType()))
      case I2F | L2F | D2F => stack.push(Conv(stack.pop(), FloatType()))
      case I2D | L2D | F2D => stack.push(Conv(stack.pop(), DoubleType()))
      case L2I | F2I | D2I => stack.push(Conv(stack.pop(), IntType()))
      case I2B => stack.push(Conv(stack.pop(), ByteType()))
      case I2C => stack.push(Conv(stack.pop(), CharType()))
      case I2S => stack.push(Conv(stack.pop(), ShortType()))

      case LCMP => stack.push(Binop("cmp", stackPop2()))
      case FCMPL | DCMPL => stack.push(Binop("cmpl", stackPop2()))
      case FCMPG | DCMPG => stack.push(Binop("cmpg", stackPop2()))
      case IRETURN | LRETURN | FRETURN | DRETURN | ARETURN => stms.append(ReturnStm(stack.pop()))
      case RETURN => stms.append(ReturnVoidStm())

      case ARRAYLENGTH => stack.push(ArrayLength(stack.pop()))

      case ATHROW => stms.append(ThrowStm(stack.pop()))
      case MONITORENTER => stms.append(MonitorEnter(stack.pop()))
      case MONITOREXIT => stms.append(MonitorExit(stack.pop()))
    }
  }
}
