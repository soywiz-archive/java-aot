import ast.{InsUtils, AstBranchAnalyzer, AstFrame}
import org.objectweb.asm.{Label, Opcodes}
import org.objectweb.asm.tree._
import org.scalatest._
import org.scalatest.matchers.Matchers
import scala.collection.mutable

class Test extends FlatSpec with Matchers {
  "test" should "test" in {
    val frame = new AstFrame()
    frame.process(List(
      new InsnNode(Opcodes.ICONST_1),
      new JumpInsnNode(Opcodes.GOTO, new LabelNode(new Label()))
    ))

    assert(frame.stack.length == 1)
  }

  /*
  "test" should "test2" in {
    val ins = InsUtils.createList(List(
      new InsnNode(Opcodes.ICONST_1),
      new JumpInsnNode(Opcodes.GOTO, new LabelNode(new Label()))
    ))

    new AstBranchAnalyzer().queue(ins.getFirst)
  }
  */

  "analyzer" should "allow ternary operator" in {
    val label1 = new LabelNode(new Label())
    val label2 = new LabelNode(new Label())

    val ins = InsUtils.createList(List(
      new InsnNode(Opcodes.ICONST_1),
      new JumpInsnNode(Opcodes.IFNE, label1),
      new InsnNode(Opcodes.ICONST_1),
      new JumpInsnNode(Opcodes.GOTO, label2),
      label1,
      new InsnNode(Opcodes.ICONST_2),
      new JumpInsnNode(Opcodes.GOTO, label2),
      label2,
      new InsnNode(Opcodes.IRETURN)
    ))

    val stms = new AstBranchAnalyzer().analyze(ins)
    println("+++++++++++++")
    stms.foreach(println)
  }

  "test" should "test3" in {
    case class Test(a:String)
    assert(List(Test("a"), Test("b")) == List(Test("a"), Test("b")))
  }
}
