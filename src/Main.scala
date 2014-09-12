import output.cpp.{ClassTreeGenerator, ClassGenerator}

object Main extends App {
  SootUtils.init()

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val generator = new ClassTreeGenerator()
  generator.enqueue("java.lang.Object")
  generator.run()

  //SootCppGenerator.doClass("java.lang.String")
}
