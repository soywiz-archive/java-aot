import target.{TargetAs3, TargetCpp}
import util.{ClassDependencyWalker, OS, RuntimeProvider, SootUtils}
import vfs.FileVfsNode

import scala.collection.mutable.ListBuffer

object Main extends App {
  var classPathList = new ListBuffer[String]
  var entryPoint = "sample1.Sample1"

  def processArgs(args:Seq[String]): Unit = {
    if (args.nonEmpty) {
      val head = args.head
      val rest = args.tail
      head match {
        case "-cp" =>
          val cp = rest.head
          classPathList.append(cp)
          //println(cp)
          processArgs(rest.tail)
        case _ =>
          entryPoint = head
          //println(head)
      }
    }
  }

  println(args)

  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")
  val runtimeProvider = new RuntimeProvider()

  SootUtils.init(runtimeProvider)

  //val target = new TargetCpp()
  val target = new TargetAs3()
  print("Calculating dependency tree...")
  val dependencies = new ClassDependencyWalker(runtimeProvider).getRefClassesTree(entryPoint)

  println("" + dependencies.length + "...Ok")
  println(s"target:${target.targetName}")

  target.buildAndRun(dependencies, entryPoint, runtimeProvider, new FileVfsNode(s"${runtimeProvider.project_root}/out_${target.targetName}"))
}