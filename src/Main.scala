import output.cpp.ClassTreeGenerator

object Main extends App {
  SootUtils.init()

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val generator = new ClassTreeGenerator()
  //generator.enqueue("java.lang.System")
  generator.enqueue("java.Simple1")
  generator.run()

  //SootCppGenerator.doClass("java.lang.String")
}
