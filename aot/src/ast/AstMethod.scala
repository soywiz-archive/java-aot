package ast

import org.objectweb.asm.tree._
import org.objectweb.asm._

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._

class AstMethod {
  def process(clazz:ClassNode, method:MethodNode):Unit = {
    println("AstMethod:" + clazz.name + " :: " + method.name)

    val locals = new ListBuffer[LValue]()
    val frameNodes = new ListBuffer[AbstractInsnNode]()

    val methodType = Type.getMethodType(method.desc)
    val clazzType = NodeUtils.typeFromType(Type.getType("L" + clazz.name + ";"))
    val isStatic = (method.access & Opcodes.ACC_STATIC) != 0

    if (!isStatic) {
      locals.append(This(clazzType))
    }
    for (local <- (methodType.getArgumentTypes, (0 until methodType.getArgumentTypes.length)).zipped.map((kind, index) => {
      Argument(NodeUtils.typeFromType(kind), index)
    })) {
      locals.append(local)
    }

    println("locals:" + locals.size)
    println("locals:" + locals)

    def flushFrame(): Unit = {
      if (frameNodes.length <= 0) return

      new AstFrame(locals.toArray).process(frameNodes)
      frameNodes.clear()
      //locals.clear()
    }

    def convert(i:Any, index:Int):LValue = {
      i match {
        case v:Int => Argument(IntType(), index)
        case null => Argument(NullType(), index)
        case _ =>
          throw new NotImplementedError()
      }
      Argument(NullType(), 0)
    }

    for (i <- method.instructions.toArray) {
      i match {
        case i:FrameNode =>
          i.`type` match {
            case Opcodes.F_NEW =>
            case Opcodes.F_FULL =>
            case Opcodes.F_APPEND =>
            case Opcodes.F_CHOP =>
            case Opcodes.F_SAME =>
            case Opcodes.F_SAME1 =>
          }
          for (n <- 0 until i.local.size()) {
            //locals(n) = convert(i.local.get(n), n)
          }
          flushFrame()
        case _ =>
          frameNodes.append(i)
      }
    }

    flushFrame()
    //process(node.instructions)
  }
}
