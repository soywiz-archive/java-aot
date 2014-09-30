import java.io.File

import org.objectweb.asm.tree._
import org.objectweb.asm.{Type, ClassReader, Opcodes, ClassVisitor}
import target.as3.As3ClassTreeGenerator
import target.{FileBytes, RuntimeProvider, SootUtils, OS}
import target.cpp.CppClassTreeGenerator
import scala.collection.JavaConverters._
import scala.collection.mutable

object Main extends App {
  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")
  val runtimeProvider = new RuntimeProvider()

  SootUtils.init(runtimeProvider)

  //ClassGenerator.doClass("Test1")
  //new ClassGenerator("java.lang.Object").doClass()

  //val generator = new As3ClassTreeGenerator()
  val generator = new CppClassTreeGenerator(runtimeProvider)

  run()

  def run(): Unit = {
    val entryPoint = "sample1.Sample1"
    val dependencies = getRefClassesTree(entryPoint)

    for (dependency <- dependencies) {
      generator.enqueue(dependency)
    }
    generator.run(entryPoint)
  }

  //SootCppGenerator.doClass("java.lang.String")

  def getRefClassesTree(path:String) : List[String] = {
    val visited = mutable.HashSet[String]()
    val toVisit = mutable.Queue[String]()

    def enqueue(path:String): Unit = {
      if (!visited.contains(path)) {
        toVisit.enqueue(path)
        visited.add(path)
      }
    }

    enqueue(path)

    while (toVisit.nonEmpty) {
      val element = toVisit.dequeue()
      //println(s"::$element")
      val items = getRefClasses(element)
      //items.map(println)
      items.map(enqueue)
    }

    visited.toList
  }

  def getRefClasses(path:String): List[String] = {
    getRefClasses(FileBytes.read(new File(runtimeProvider.getClassPath(path))))
  }

  def getRefClasses(data:Array[Byte]): List[String] = {
    val cn = new ClassNode(Opcodes.ASM5)

    val cr = new ClassReader(data)
    cr.accept(cn, 0)

    val classNames = mutable.HashSet[String]()

    def processType(kind:Type): Unit = {
      if (kind.getSort == Type.ARRAY) {
        processType(kind.getElementType)
      } else {
        kind.getSort match {
          case Type.INT | Type.CHAR | Type.BYTE | Type.BOOLEAN | Type.VOID | Type.SHORT | Type.LONG | Type.DOUBLE | Type.FLOAT =>
          case _ =>
            val name = kind.getClassName
            if (name != null) {
              classNames.add(name)
            }
        }
      }
    }

    def processMethodType(kind:Type): Unit = {
      processType(kind.getReturnType)
      for (arg <- kind.getArgumentTypes) processType(arg)
    }

    if (cn.superName != null) {
      processType(Type.getType(cn.superName))
    }

    for (interfaceName <- cn.interfaces.asScala.map(_.asInstanceOf[String])) {
      processType(Type.getType(interfaceName))
    }

    for (innerClass <- cn.innerClasses.asScala.map(_.asInstanceOf[InnerClassNode])) {
      processType(Type.getType(innerClass.name))
    }

    for (field <- cn.fields.asScala.map(_.asInstanceOf[FieldNode])) {
      processType(Type.getType(field.desc))
    }

    //val debug = (cn.name == "java/lang/System")

    for (method <- cn.methods.asScala.map(_.asInstanceOf[MethodNode])) {
      //if (debug) println(s"   - ${method.name}")
      processMethodType(Type.getType(method.desc))
      for (ins <- method.instructions.toArray) {
        //if (method.name == "children") println("  " + ins)
        ins match {
          case i:TypeInsnNode =>
            //if (method.name == "children") println("  + " + i.desc + " : " + Type.getType(s"L${i.desc};").getClassName)
            if (i.desc.startsWith("[")) {
              processType(Type.getType(i.desc))
            } else {
              processType(Type.getType(s"L${i.desc};"))
            }
          case i:MethodInsnNode =>
            //if (method.name == "children") println("  + " + i.desc)
            processMethodType(Type.getType(i.desc))
          case i:MultiANewArrayInsnNode =>
            //if (method.name == "children") println("  + " + i.desc)
            processMethodType(Type.getType(i.desc))
          case i:FieldInsnNode =>
            //if (method.name == "children") println("  + " + i.owner + " : " + i.desc)
            processType(Type.getType("L" + i.owner + ";"))
            processType(Type.getType(i.desc))
          case _ =>
        }
      }
    }

    classNames.toList
    //classNames.map(println)
  }
}
