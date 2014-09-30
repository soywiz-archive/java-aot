import target.as3.As3ClassTreeGenerator
import target.{RuntimeProvider, SootUtils, OS}
import target.cpp.CppClassTreeGenerator

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")
  val runtimeProvider = new RuntimeProvider()

  SootUtils.init(runtimeProvider)

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  //val generator = new As3ClassTreeGenerator()
  val generator = new CppClassTreeGenerator(runtimeProvider)

  //generator.enqueue("java.lang.System")
  //generator.enqueue("java.lang.Exception")
  //generator.enqueue("libgame.SDLApi")
  generator.enqueue("java.util.LinkedList")
  generator.enqueue("java.lang.Math")
  generator.enqueue("java.io.InputStream")
  generator.enqueue("jflash.util.Size")
  generator.enqueue("jflash.util.SignalHandler")
  generator.enqueue("jflash.backend.TextureBase")
  generator.enqueue("jflash.display.MouseUpdate")
  generator.enqueue("jflash.display.DisplayObject")
  generator.enqueue("jflash.display.DisplayObjectContainer")
  generator.enqueue("libcore.StdoutOutputStream")
  generator.enqueue("libcore.Native")
  generator.enqueue("libgame.SDL")
  generator.enqueue("libgame.GL")
  generator.enqueue("sample1.Sample1")
  generator.run("sample1.Sample1")

  //SootCppGenerator.doClass("java.lang.String")
}
