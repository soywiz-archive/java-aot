import target.{SootUtils, OS}
import target.cpp.CppClassTreeGenerator

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")

  SootUtils.init()

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val generator = new CppClassTreeGenerator()
  //generator.enqueue("java.lang.System")
  generator.enqueue("libgame.SDLApi")
  generator.enqueue("libcore.StdoutOutputStream")
  generator.enqueue("libcore.Native")
  generator.enqueue("java.Simple1")
  generator.run()

  //SootCppGenerator.doClass("java.lang.String")
}
