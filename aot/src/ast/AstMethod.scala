package ast

import _root_.java.util

import org.objectweb.asm.tree._
import org.objectweb.asm._

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class AstMethod {
  def process(clazz:ClassNode, method:MethodNode):Unit = {
    println("AstMethod:" + clazz.name + " :: " + method.name + " :: " + method.desc)
    println("++++++++++++++++++++++++++++++++++++++++++++++++++")

    val instructions = method.instructions
    var lastFrame:FrameNode = null

    for (local <- method.localVariables.asScala.map(_.asInstanceOf[LocalVariableNode])) {
      method.instructions.insertBefore(local.start, new LocalDefNode(local))
      //println(local.name + ":" + local.index + ":" + local.desc)
    }

    val locals = new Array[LValue](method.maxLocals)
    val frameNodes = new ListBuffer[AbstractInsnNode]()

    val methodType = Type.getMethodType(method.desc)
    val clazzType = NodeUtils.typeFromType(Type.getType("L" + clazz.name + ";"))
    val isStatic = (method.access & Opcodes.ACC_STATIC) != 0

    /*
    if (!isStatic) {
      locals.append(This(clazzType))
    }
    for (local <- (methodType.getArgumentTypes, (0 until methodType.getArgumentTypes.length)).zipped.map((kind, index) => {
      Argument(NodeUtils.typeFromType(kind), index)
    })) {
      locals.append(local)
    }
    */

    def flushFrame(): Unit = {
      if (frameNodes.length <= 0) return

      val i = lastFrame
      val frameTypeString = if (i == null) "null" else i.`type` match {
        case Opcodes.F_NEW => "F_new"
        case Opcodes.F_FULL => "F_full"
        case Opcodes.F_APPEND => "F_append"
        case Opcodes.F_CHOP => "F_chop"
        case Opcodes.F_SAME => "F_same"
        case Opcodes.F_SAME1 => "F_same1"
      }

      val localSize = if (i != null && i.local != null) i.local.size() else 0
      val stackSize = if (i != null && i.stack != null) i.stack.size() else 0

      println("--------------------------")
      println(s"FRAME:$frameTypeString,locals:$localSize,stack:$stackSize")

      new AstFrame(locals.toArray).process(frameNodes)
      frameNodes.clear()
      lastFrame = null
      //locals.clear()
    }

    for (i <- method.instructions.toArray) {
      i match {
        case i:LocalDefNode =>
          locals(i.local.index) = Local(NodeUtils.typeFromDesc(i.local.desc), i.local.index, i.local.name)
        case i:FrameNode =>
          lastFrame = i
          flushFrame()
        case _ =>
          frameNodes.append(i)
      }
    }

    flushFrame()
    //process(node.instructions)
  }
}

class LocalDefNode(val local:LocalVariableNode) extends AbstractInsnNode(-123) {
  override def getType: Int = -123
  override def clone(labels: util.Map[_, _]): AbstractInsnNode = this
  override def accept(cv: MethodVisitor): Unit = { }
}