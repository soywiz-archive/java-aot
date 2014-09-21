package output.cpp

import java.io._
import java.nio.charset.Charset

import com.google.common.io.{ByteStreams, Files}
import soot.{Scene, SootClass}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source

class ClassTreeGenerator {
  private val processedList = new mutable.HashSet[SootClass]
  private val toProcessList = new mutable.Queue[SootClass]

  def enqueue(className: String): scala.Unit = {
    enqueue(Scene.v.loadClassAndSupport(className))
  }

  def enqueue(clazz: SootClass): scala.Unit = {
    if (!processedList.contains(clazz)) {
      processedList.add(clazz)
      toProcessList.enqueue(clazz)
    }
  }

  def run() = {

    val outputPath = System.getProperty("java.io.tmpdir")

    val cl = this.getClass.getClassLoader
    Files.write(ByteStreams.toByteArray(cl.getResourceAsStream("types.cpp")), new File(outputPath + "/types.cpp"))
    Files.write(ByteStreams.toByteArray(cl.getResourceAsStream("types.h")), new File(outputPath + "/types.h"))

    val frameworks = new mutable.HashSet[String]

    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      println("Processing class: " + item.getName)
      val result = new ClassGenerator(item).doClass()

      if (result.nativeFramework != null) frameworks.add(result.nativeFramework)

      Files.write(result.declaration, new File(outputPath + "/" + Mangling.mangleFullClassName(item.getName) + ".h"), Charset.forName("UTF-8"))
      Files.write(result.definition, new File(outputPath + "/" + Mangling.mangleFullClassName(item.getName) + ".cpp"), Charset.forName("UTF-8"))

      //println(result.definition)
      //println(result.declaration)
      for (referencedClass <- result.referencedClasses) {
        enqueue(referencedClass)
        //println(s"  Referencing: ${referencedClass.getName}");
      }
    }
    println("Processed classes: " + processedList.size)

    Files.write("#include \"java_Simple1.h\" \n int main(int argc, char **argv) { printf(\"Start!\\n\"); java_Simple1::main(new Array<java_lang_String*>((java_lang_String**)0, 0)); return 0; }", new File(outputPath + "/main.cpp"), Charset.forName("UTF-8"))
    val paths = processedList.filter(_.getName != "java.lang.Object").map(item => Mangling.mangleFullClassName(item.getName) + ".cpp").mkString(" ")
    Files.write("@g++ -fpermissive -Wint-to-pointer-cast -g -ggdb -gstabs -gpubnames types.cpp main.cpp " + paths, new File(outputPath + "/build.bat"), Charset.forName("UTF-8"))
    Files.write("g++ -fpermissive -Wint-to-pointer-cast -O3 types.cpp main.cpp " + paths, new File(outputPath + "/build.sh"), Charset.forName("UTF-8"))

    val frameworksAppend = frameworks.map(framework => s"-I/Library/Frameworks/${framework}.framework/Headers -framework $framework").mkString(" ")
    val command = s"g++ -fpermissive -Wint-to-pointer-cast -O3 types.cpp main.cpp $paths -F/Library/Frameworks $frameworksAppend -framework Cocoa"

    println(command)
    new File(outputPath + "/a.out").delete()
    if (redirectProcess(Runtime.getRuntime.exec(command, new Array[String](0), new File(outputPath))) == 0) {
      redirectProcess(Runtime.getRuntime.exec( "./a.out", new Array[String](0), new File(outputPath)))
    }
  }

  private def redirectProcess(p:Process): Int = {
    for (line <- Source.fromInputStream(p.getInputStream).getLines()) {
      println(line)
    }

    for (line <- Source.fromInputStream(p.getErrorStream).getLines()) {
      println(line)
    }

    p.waitFor()
  }
}
