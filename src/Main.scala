import output.SootUtils
import output.cpp.{ClassTreeGenerator, ClassGenerator}

object Main extends App {
  SootUtils.init()

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  val generator = new ClassTreeGenerator()
  //generator.enqueue("sun.reflect.Reflection")
  //generator.enqueue("sun.misc.SharedSecrets")
  //generator.enqueue("sun.misc.FloatingDecimal")
  //generator.enqueue("java.lang.StrictMath")
  //generator.enqueue("java.lang.Integer$IntegerCache")
  generator.enqueue("java.lang.Object")
  generator.run()

  //SootCppGenerator.doClass("java.lang.String")
}
