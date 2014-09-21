import output.cpp.ClassTreeGenerator

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println("OS current temporary directory is "
    + System.getProperty("java.io.tmpdir"))

  SootUtils.init()

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val generator = new ClassTreeGenerator()
  //generator.enqueue("java.lang.System")
  generator.enqueue("libcore.StdoutOutputStream")
  generator.enqueue("libcore.Native")
  generator.enqueue("java.Simple1")
  generator.run()

  //SootCppGenerator.doClass("java.lang.String")
}
