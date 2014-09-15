package output.cpp

import java.io.File
import java.nio.charset.Charset

import com.google.common.io.Files
import soot.{Scene, SootClass}

import scala.collection.mutable

class ClassTreeGenerator {
  private val processedList = new mutable.HashSet[SootClass]
  private val toProcessList = new mutable.Queue[SootClass]

  def enqueue(className: String): scala.Unit = {
    enqueue(Scene.v.loadClassAndSupport(className))
  }

  def enqueue(clazz: SootClass): scala.Unit = {
    if (!processedList.contains(clazz)) toProcessList.enqueue(clazz)
  }

  def run() = {
    val outputPath = "c:\\temp"

    Files.copy(new File("c:\\projects\\java-aot\\java_runtime\\cpp\\types.h"), new File(outputPath + "\\types.h"))
    Files.copy(new File("c:\\projects\\java-aot\\java_runtime\\cpp\\types.cpp"), new File(outputPath + "\\types.cpp"))

    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      processedList.add(item)
      println("Processing class: " + item.getName)
      val result = new ClassGenerator(item).doClass()

      Files.write(result.declaration, new File(outputPath + "/" + Mangling.mangleFullClassName(item.getName) + ".h"), Charset.forName("UTF-8"))
      Files.write(result.definition, new File(outputPath + "/" + Mangling.mangleFullClassName(item.getName) + ".cpp"), Charset.forName("UTF-8"))

      //println(result.definition)
      //println(result.declaration)
      for (referencedClass <- result.referencedClasses) enqueue(referencedClass)
    }
    println("Processed classes: " + processedList.size)

    Files.write("#include \"java_Simple1.h\" \n int main(int argc, char **argv) { printf(\"Start!\\n\"); java_Simple1::main(new Array<java_lang_String*>((java_lang_String**)0, 0)); return 0; }", new File("c:\\temp\\main.cpp"), Charset.forName("UTF-8"))
    val paths = processedList.filter(_.getName != "java.lang.Object").map(item => Mangling.mangleFullClassName(item.getName) + ".cpp").mkString(" ")
    Files.write("@g++ -fpermissive -Wint-to-pointer-cast -g -ggdb -gstabs -gpubnames types.cpp main.cpp " + paths, new File("c:\\temp\\build.bat"), Charset.forName("UTF-8"))
  }
}
