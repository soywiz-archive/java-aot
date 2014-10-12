package util

import java.io.File

import ast.{InsUtils, AstMethod}
import org.objectweb.asm.{Type, ClassReader, Opcodes}
import org.objectweb.asm.tree._
import scala.collection.JavaConverters._

import scala.collection.mutable

class ClassDependencyWalker(runtimeProvider:RuntimeProvider) {

  def getRefClassesTree(path:String) : Seq[String] = {
    val visited = mutable.HashSet[String]()
    val toVisit = mutable.Queue[String]()

    def enqueue(path:String): Unit = {
      if (!visited.contains(path)) {
        //println(path)
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

  private def getRefClasses(path:String): List[String] = getRefClasses(runtimeProvider.getClassVfsNode(path).read())

  private def getRefClasses(data:Array[Byte]): List[String] = {
    val cn = new ClassNode(Opcodes.ASM5)

    val cr = new ClassReader(data)
    //cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)
    //cr.accept(cn, ClassReader.SKIP_FRAMES)
    cr.accept(cn, ClassReader.EXPAND_FRAMES)

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
              //println("   - " + name)
              classNames.add(name)
            }
        }
      }
    }

    def processMethodType(kind:Type): Unit = {
      processType(kind.getReturnType)
      for (arg <- kind.getArgumentTypes) processType(arg)
    }

    def processClassName(className:String) = processType(Type.getType("L" + className + ";"))

    if (cn.superName != null) processClassName(cn.superName)
    for (interfaceName <- cn.interfaces.asScala.map(_.asInstanceOf[String])) processClassName(interfaceName)
    for (innerClass <- cn.innerClasses.asScala.map(_.asInstanceOf[InnerClassNode])) processClassName(innerClass.name)
    for (field <- cn.fields.asScala.map(_.asInstanceOf[FieldNode])) processType(Type.getType(field.desc))

    //val debug = (cn.name == "java/lang/System")

    //println(cn.name)
    for (method <- cn.methods.asScala.map(_.asInstanceOf[MethodNode])) {
      //println("---------------------------------------------")
      //println(s"Analyzing: ${cn.name} :: ${method.name}")
      //for (node <- method.instructions.toArray) println(InsUtils.toString(node))
      //new AstMethod().process(cn, method)

      //if (debug) println(s"   - ${method.name}")
      //println(" - " + method.name)
      processMethodType(Type.getType(method.desc))

      for (ins <- method.instructions.toArray) {
        //if (method.name == "children") println("  " + ins)
        ins match {
          case i:TypeInsnNode =>
            //if (method.name == "children") println("  + " + i.desc + " : " + Type.getType(s"L${i.desc};").getClassName)
            if (i.desc.startsWith("[")) {
              processType(Type.getType(i.desc))
            } else {
              processClassName(i.desc)
            }
          case i:MethodInsnNode =>  processClassName(i.owner)
          case i:MultiANewArrayInsnNode =>  processMethodType(Type.getType(i.desc))
          case i:FieldInsnNode =>
            //if (method.name == "children") println("  + " + i.owner + " : " + i.desc)
            processClassName(i.owner)
            processType(Type.getType(i.desc))
          case i:LabelNode =>
          case i:LineNumberNode =>
          case i:LdcInsnNode =>
          case i:VarInsnNode =>
          case _ =>
            //println(ins)
        }
      }
    }

    classNames.toList
    //classNames.map(println)
  }
}
