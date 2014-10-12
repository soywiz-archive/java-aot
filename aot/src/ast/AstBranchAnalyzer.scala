package ast

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

// TODO: Analyzer should not call frame to generate, in order to be two separated steps
class AstBranchAnalyzer {
  val map = new mutable.HashMap[AbstractInsnNode, AstBranch]
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
      if (optframe.get.input != input) {
        println("-------------------------- Label: " + initial)
        println("Previous Stack Input: " + optframe.get.input)
        println("Used Stack Input: " + input)
      }
      assert(optframe.get.input == input)
    } else {
      val frame = analyze(initial, input)
      println("<---- " + frame.output + " | " + frame.input)
      map(initial) = frame
    }

  }

  private def queue(initial:AbstractInsnNode, input:InoutFrame = InoutFrame(List())) = {
    exploreQueue.enqueue((initial, input))
    this
  }

  private def analyze(initial:AbstractInsnNode, input:InoutFrame): AstBranch = {
    println("----------------------------------")
    println(s"Analyzing branch: $initial <-- $input")
    var node = initial

    val nodes = new ListBuffer[AbstractInsnNode]

    // Remove contiguous LabelNodes
    while (node.isInstanceOf[LabelNode]) {
      nodes.append(node)
      node = node.getNext
    }

    def process(nextList:Seq[AbstractInsnNode], hasGoto:Boolean): AstBranch = {
      val frame = new AstFrame(null, input.locals)
      frame.process(nodes)
      val stms = frame.stms.toList


      val outputStack = frame.stack.toList
      val output = InoutFrame.forStack(outputStack)
      println(s" Output --> $output")

      val stms0 = if (hasGoto) stms.dropRight(1) else stms
      val stms1 = (output.locals, outputStack).zipped.map((local, value) => Assign(local, value))
      val stms2 = if (hasGoto) stms.takeRight(1) else List()

      for (next <- nextList) queue(next, output)
      AstBranch(nodes.toList, output, input, stms0 ::: stms1 ::: stms2)
    }

    while (node != null) {
      if (node.isInstanceOf[LabelNode]) return process(List(node), hasGoto = false)
      nodes.append(node)
      node match {
        case i:JumpInsnNode =>
          val unconditional = i.getOpcode == Opcodes.GOTO
          return process((if (unconditional) List() else List(i.getNext)) ::: List(i.label), hasGoto = true)
        case _ =>
      }

      node = node.getNext
    }

    process(List(), hasGoto = false)
  }
}

object InoutFrame {
  def forStack(stack:List[Expr]) = InoutFrame(stack.map(_.getType))
}

case class InoutFrame(sharedStack:List[NodeType]) {
  lazy val locals = sharedStack.map(Local(_, -1, "temp_stack"))
}
case class AstBranch(nodes:List[AbstractInsnNode], output:InoutFrame, input:InoutFrame, stms:List[Stm])