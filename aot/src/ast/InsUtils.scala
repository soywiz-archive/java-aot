package ast

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

object InsUtils {
  def createList(nodes:Seq[AbstractInsnNode]) = {
    val list = new InsnList()
    for (node <- nodes) {
      list.add(node)
    }
    list
  }

  def toString(node:AbstractInsnNode) = {
    val opcodeName = if (nameMap.contains(node.getOpcode)) nameMap(node.getOpcode) else s"$node"
     node match {
      case i:JumpInsnNode => s"JumpInsnNode($opcodeName, ${i.label})"
      case i:LabelNode => s"$i"
      case i:LineNumberNode => s"LineNumberNode(${i.line}, ${i.start})"
      case i:VarInsnNode => s"VarInsnNode($opcodeName, ${i.`var`})"
      case i:MethodInsnNode => s"MethodInsnNode($opcodeName, ${i.owner}, ${i.name}, ${i.desc})"
      case i:InsnNode => s"InsnNode($opcodeName)"
      case i:FieldInsnNode => s"FieldInsnNode($opcodeName, ${i.owner}, ${i.name}, ${i.desc})"
      case i:LdcInsnNode => s"LdcInsnNode(${i.cst})"
      case i:FrameNode =>
        val localTypes = i.local.toArray.toList
        val stackTypes = i.stack.toArray.toList
        s"FrameNode(${i.`type`}, locals:${i.local.size()}($localTypes), stack:${i.stack.size()}($stackTypes))"
      case i:IntInsnNode => s"IntInsnNode($opcodeName, ${i.operand})"
      case i:TypeInsnNode => s"TypeInsnNode($opcodeName, ${i.desc})"
      case i:IincInsnNode => s"IincInsnNode($opcodeName, ${i.`var`}, ${i.incr})"
    }
  }

  val nameMap = Map(
    Opcodes.NOP -> "NOP",
    Opcodes.ACONST_NULL -> "ACONST_NULL",
    Opcodes.ICONST_M1 -> "ICONST_M1",
    Opcodes.ICONST_0 -> "ICONST_0",
    Opcodes.ICONST_1 -> "ICONST_1",
    Opcodes.ICONST_2 -> "ICONST_2",
    Opcodes.ICONST_3 -> "ICONST_3",
    Opcodes.ICONST_4 -> "ICONST_4",
    Opcodes.ICONST_5 -> "ICONST_5",
    Opcodes.LCONST_0 -> "LCONST_0",
    Opcodes.LCONST_1 -> "LCONST_1",
    Opcodes.FCONST_0 -> "FCONST_0",
    Opcodes.FCONST_1 -> "FCONST_1",
    Opcodes.FCONST_2 -> "FCONST_2",
    Opcodes.DCONST_0 -> "DCONST_0",
    Opcodes.DCONST_1 -> "DCONST_1",
    Opcodes.BIPUSH -> "BIPUSH",
    Opcodes.SIPUSH -> "SIPUSH",
    Opcodes.LDC -> "LDC",
    Opcodes.ILOAD -> "ILOAD",
    Opcodes.LLOAD -> "LLOAD",
    Opcodes.FLOAD -> "FLOAD",
    Opcodes.DLOAD -> "DLOAD",
    Opcodes.ALOAD -> "ALOAD",
    Opcodes.IALOAD -> "IALOAD",
    Opcodes.LALOAD -> "LALOAD",
    Opcodes.FALOAD -> "FALOAD",
    Opcodes.DALOAD -> "DALOAD",
    Opcodes.AALOAD -> "AALOAD",
    Opcodes.BALOAD -> "BALOAD",
    Opcodes.CALOAD -> "CALOAD",
    Opcodes.SALOAD -> "SALOAD",
    Opcodes.ISTORE -> "ISTORE",
    Opcodes.LSTORE -> "LSTORE",
    Opcodes.FSTORE -> "FSTORE",
    Opcodes.DSTORE -> "DSTORE",
    Opcodes.ASTORE -> "ASTORE",
    Opcodes.IASTORE -> "IASTORE",
    Opcodes.LASTORE -> "LASTORE",
    Opcodes.FASTORE -> "FASTORE",
    Opcodes.DASTORE -> "DASTORE",
    Opcodes.AASTORE -> "AASTORE",
    Opcodes.BASTORE -> "BASTORE",
    Opcodes.CASTORE -> "CASTORE",
    Opcodes.SASTORE -> "SASTORE",
    Opcodes.POP -> "POP",
    Opcodes.POP2 -> "POP2",
    Opcodes.DUP -> "DUP",
    Opcodes.DUP_X1 -> "DUP_X1",
    Opcodes.DUP_X2 -> "DUP_X2",
    Opcodes.DUP2 -> "DUP2",
    Opcodes.DUP2_X1 -> "DUP2_X1",
    Opcodes.DUP2_X2 -> "DUP2_X2",
    Opcodes.SWAP -> "SWAP",
    Opcodes.IADD -> "IADD",
    Opcodes.LADD -> "LADD",
    Opcodes.FADD -> "FADD",
    Opcodes.DADD -> "DADD",
    Opcodes.ISUB -> "ISUB",
    Opcodes.LSUB -> "LSUB",
    Opcodes.FSUB -> "FSUB",
    Opcodes.DSUB -> "DSUB",
    Opcodes.IMUL -> "IMUL",
    Opcodes.LMUL -> "LMUL",
    Opcodes.FMUL -> "FMUL",
    Opcodes.DMUL -> "DMUL",
    Opcodes.IDIV -> "IDIV",
    Opcodes.LDIV -> "LDIV",
    Opcodes.FDIV -> "FDIV",
    Opcodes.DDIV -> "DDIV",
    Opcodes.IREM -> "IREM",
    Opcodes.LREM -> "LREM",
    Opcodes.FREM -> "FREM",
    Opcodes.DREM -> "DREM",
    Opcodes.INEG -> "INEG",
    Opcodes.LNEG -> "LNEG",
    Opcodes.FNEG -> "FNEG",
    Opcodes.DNEG -> "DNEG",
    Opcodes.ISHL -> "ISHL",
    Opcodes.LSHL -> "LSHL",
    Opcodes.ISHR -> "ISHR",
    Opcodes.LSHR -> "LSHR",
    Opcodes.IUSHR -> "IUSHR",
    Opcodes.LUSHR -> "LUSHR",
    Opcodes.IAND -> "IAND",
    Opcodes.LAND -> "LAND",
    Opcodes.IOR -> "IOR",
    Opcodes.LOR -> "LOR",
    Opcodes.IXOR -> "IXOR",
    Opcodes.LXOR -> "LXOR",
    Opcodes.IINC -> "IINC",
    Opcodes.I2L -> "I2L",
    Opcodes.I2F -> "I2F",
    Opcodes.I2D -> "I2D",
    Opcodes.L2I -> "L2I",
    Opcodes.L2F -> "L2F",
    Opcodes.L2D -> "L2D",
    Opcodes.F2I -> "F2I",
    Opcodes.F2L -> "F2L",
    Opcodes.F2D -> "F2D",
    Opcodes.D2I -> "D2I",
    Opcodes.D2L -> "D2L",
    Opcodes.D2F -> "D2F",
    Opcodes.I2B -> "I2B",
    Opcodes.I2C -> "I2C",
    Opcodes.I2S -> "I2S",
    Opcodes.LCMP -> "LCMP",
    Opcodes.FCMPL -> "FCMPL",
    Opcodes.FCMPG -> "FCMPG",
    Opcodes.DCMPL -> "DCMPL",
    Opcodes.DCMPG -> "DCMPG",
    Opcodes.IFEQ -> "IFEQ",
    Opcodes.IFNE -> "IFNE",
    Opcodes.IFLT -> "IFLT",
    Opcodes.IFGE -> "IFGE",
    Opcodes.IFGT -> "IFGT",
    Opcodes.IFLE -> "IFLE",
    Opcodes.IF_ICMPEQ -> "IF_ICMPEQ",
    Opcodes.IF_ICMPNE -> "IF_ICMPNE",
    Opcodes.IF_ICMPLT -> "IF_ICMPLT",
    Opcodes.IF_ICMPGE -> "IF_ICMPGE",
    Opcodes.IF_ICMPGT -> "IF_ICMPGT",
    Opcodes.IF_ICMPLE -> "IF_ICMPLE",
    Opcodes.IF_ACMPEQ -> "IF_ACMPEQ",
    Opcodes.IF_ACMPNE -> "IF_ACMPNE",
    Opcodes.GOTO -> "GOTO",
    Opcodes.JSR -> "JSR",
    Opcodes.RET -> "RET",
    Opcodes.TABLESWITCH -> "TABLESWITCH",
    Opcodes.LOOKUPSWITCH -> "LOOKUPSWITCH",
    Opcodes.IRETURN -> "IRETURN",
    Opcodes.LRETURN -> "LRETURN",
    Opcodes.FRETURN -> "FRETURN",
    Opcodes.DRETURN -> "DRETURN",
    Opcodes.ARETURN -> "ARETURN",
    Opcodes.RETURN -> "RETURN",
    Opcodes.GETSTATIC -> "GETSTATIC",
    Opcodes.PUTSTATIC -> "PUTSTATIC",
    Opcodes.GETFIELD -> "GETFIELD",
    Opcodes.PUTFIELD -> "PUTFIELD",
    Opcodes.INVOKEVIRTUAL -> "INVOKEVIRTUAL",
    Opcodes.INVOKESPECIAL -> "INVOKESPECIAL",
    Opcodes.INVOKESTATIC -> "INVOKESTATIC",
    Opcodes.INVOKEINTERFACE -> "INVOKEINTERFACE",
    Opcodes.INVOKEDYNAMIC -> "INVOKEDYNAMIC",
    Opcodes.NEW -> "NEW",
    Opcodes.NEWARRAY -> "NEWARRAY",
    Opcodes.ANEWARRAY -> "ANEWARRAY",
    Opcodes.ARRAYLENGTH -> "ARRAYLENGTH",
    Opcodes.ATHROW -> "ATHROW",
    Opcodes.CHECKCAST -> "CHECKCAST",
    Opcodes.INSTANCEOF -> "INSTANCEOF",
    Opcodes.MONITORENTER -> "MONITORENTER",
    Opcodes.MONITOREXIT -> "MONITOREXIT",
    Opcodes.MULTIANEWARRAY -> "MULTIANEWARRAY",
    Opcodes.IFNULL -> "IFNULL",
    Opcodes.IFNONNULL -> "IFNONNULL"
  )
}
