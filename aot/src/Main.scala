import target.{TargetAs3, TargetCpp}
import util.{ClassDependencyWalker, OS, RuntimeProvider, SootUtils}
import vfs.FileVfsNode

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")
  val runtimeProvider = new RuntimeProvider()

  SootUtils.init(runtimeProvider)

  val entryPoint = "sample1.Sample1"

  //val target = new TargetCpp()
  val target = new TargetAs3()
  print("Calculating dependency tree...")
  val dependencies = new ClassDependencyWalker(runtimeProvider).getRefClassesTree(entryPoint)

  println("" + dependencies.length + "...Ok")
  println(s"target:${target.targetName}")

  target.buildAndRun(dependencies, entryPoint, runtimeProvider, new FileVfsNode(s"${runtimeProvider.project_root}/out_${target.targetName}"))
}