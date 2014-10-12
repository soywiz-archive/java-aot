package ast

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.tree._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

// TODO: Analyzer should not call frame to generate, in order to be two separated steps
class AstBranchAnalyzer(method:MethodNode = null) {
  val context = new AstMethodContext(method)
  val map = new mutable.HashMap[AbstractInsnNode, AstBranch]
  val mapInput = new mutable.HashMap[AbstractInsnNode, InoutFrame]
  var exploreQueue = new mutable.Queue[(AbstractInsnNode, InoutFrame)]()

  def analyze(list:InsnList): List[Stm] = {
    var stms = List[Stm]()
    queue(list.getFirst).explore()
    for (item <- list.toArray) {
      val item2 = map.get(item)
      if (item2.isDefined) {
        stms = stms ::: item2.get.stms
      }
    }
    stms
  }

  private def explore(): Unit = {
    while (exploreQueue.nonEmpty) {
      val (initial, input) = exploreQueue.dequeue()
      _explore(initial, input)
    }
  }

  private def _explore(initial:AbstractInsnNode, input:InoutFrame): Unit = {
    val optframe = map.get(initial)
    if (optframe.isDefined) {
      ensureInoutCompatible(optframe.get.input, input.sharedStack)
    } else {
      val frame = analyze(initial, input)
      if (context.debug) println("<---- " + frame.output + " | " + frame.input)
      map(initial) = frame
    }

  }

  private def queue(initial:AbstractInsnNode, input:InoutFrame = InoutFrame(List(), List())) = {
    exploreQueue.enqueue((initial, input))
    this
  }

  private def ensureInoutCompatible(l:InoutFrame, r:List[NodeType]): Unit = {
    if (l.sharedStack != r) {
      println("--------------------------")
      println("Previous Stack Input: " + l.sharedStack)
      println("Used Stack Input: " + r)
    }
    assert(l.sharedStack == r)

  }

  private def createInout(outputStack:List[Expr], nextList:Seq[AbstractInsnNode]): InoutFrame = {
    val outputStackType = outputStack.map(_.getType)
    var inout:InoutFrame = null
    for (next <- nextList) {
      if (mapInput.contains(next)) {
        inout = mapInput(next)
        ensureInoutCompatible(inout, outputStackType)
      }
    }
    if (inout == null) {
      InoutFrame.forStack(context, outputStack)
    } else {
      inout
    }
  }

  private def analyze(initial:AbstractInsnNode, input:InoutFrame): AstBranch = {
    if (context.debug) {
      println("----------------------------------")
      println(s"Analyzing branch: $initial <-- $input")
    }
    var node = initial

    val nodes = new ListBuffer[AbstractInsnNode]

    // Remove contiguous LabelNodes
    while (node.isInstanceOf[LabelNode]) {
      nodes.append(node)
      node = node.getNext
    }

    def process(nextList:Seq[AbstractInsnNode], hasGoto:Boolean): AstBranch = {
      val frame = new AstFrame(context, null, input.locals)
      frame.process(nodes)
      val stms = frame.stms.toList


      val outputStack = frame.stack.toList
      val output = createInout(outputStack, nextList)
      if (context.debug) println(s" Output --> $output")

      val stms0 = if (hasGoto) stms.dropRight(1) else stms
      val stms1 = (output.locals, outputStack).zipped.map((local, value) => Assign(local, value))
      val stms2 = if (hasGoto) stms.takeRight(1) else List()

      for (next <- nextList) {
        mapInput(next) = output
        queue(next, output)
      }
      AstBranch(nodes.toList, output, input, stms0 ::: stms1 ::: stms2)
    }

    while (node != null) {
      if (node.isInstanceOf[LabelNode]) return process(List(node), hasGoto = false)
      nodes.append(node)

      node match {
        case i:JumpInsnNode =>
          node.getOpcode match {
            case GOTO =>
              return process(List(i.getNext), hasGoto = true)
            case _ =>
              return process(List(i.getNext, i.label), hasGoto = true)
          }
        case _ =>
          node.getOpcode match {
            // Stop analyzing
            case ATHROW | RETURN | ARETURN | IRETURN | LRETURN | FRETURN | DRETURN =>
              return process(List(), hasGoto = false)

            case _ =>
          }
      }

      node = node.getNext
    }

    process(List(), hasGoto = false)
  }
}

object InoutFrame {
  def forStack(context:AstMethodContext, stack:List[Expr]) = {
    InoutFrame(stack.map(_.getType), stack.map(kind => {
      context.allocLocal(kind.getType)
    }))
  }
}

case class InoutFrame(sharedStack:List[NodeType], locals:List[Local]) {
  //lazy val locals = sharedStack.map(Local(_, -1, "temp_stack"))
}
case class AstBranch(nodes:List[AbstractInsnNode], output:InoutFrame, input:InoutFrame, stms:List[Stm])