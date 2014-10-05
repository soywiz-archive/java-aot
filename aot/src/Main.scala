import target.TargetCpp
import util.{ClassDependencyWalker, OS, RuntimeProvider, SootUtils}

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")
  val runtimeProvider = new RuntimeProvider()

  SootUtils.init(runtimeProvider)

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val entryPoint = "sample1.Sample1"

  //val generator = new As3ClassTreeGenerator()
  val target = new TargetCpp()
  val dependencies = new ClassDependencyWalker(runtimeProvider).getRefClassesTree(entryPoint)
  target.buildAndRun(dependencies, entryPoint, runtimeProvider, runtimeProvider.cpp_classes_path)
}