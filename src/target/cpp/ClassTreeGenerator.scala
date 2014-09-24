package target.cpp

import java.io._
import java.nio.charset.Charset

import build.BuildMacOS
import com.google.common.io.{ByteStreams, Files}
import soot.{Scene, SootClass}
import target.OS

import scala.collection.mutable
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
    val file_separator = System.getProperty("file.separator")
    var java_macos_embedded_frameworks = "types\\.cpp$".r.replaceAllIn(cl.getResource("types.cpp").getPath, "/frameworks".replace("/", file_separator))
    if (OS.isWindows) java_macos_embedded_frameworks = "^/+".r.replaceAllIn(java_macos_embedded_frameworks, "")

    Files.write(ByteStreams.toByteArray(cl.getResourceAsStream("types.cpp")), new File(outputPath + "/types.cpp"))
    Files.write(ByteStreams.toByteArray(cl.getResourceAsStream("types.h")), new File(outputPath + "/types.h"))
    val png512 = ByteStreams.toByteArray(cl.getResourceAsStream("emptyicon.png"))

    val frameworks = new mutable.HashSet[String]
    val libraries = new mutable.HashSet[String]
    val cflagsList = new mutable.ListBuffer[String]

    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      println("Processing class: " + item.getName)
      val result = new ClassGenerator(item).doClass()

      if (result.nativeFramework != null) frameworks.add(result.nativeFramework)
      if (result.nativeLibrary != null) libraries.add(result.nativeLibrary)
      if (result.cflags != null) cflagsList.append(result.cflags)

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

    var frameworksAppend = ""
    if (OS.isMac) {
      frameworksAppend = frameworks.map(framework => {
        var result: String = null
        for (frameworkPath <- List(s"/Library/Frameworks/${framework}.framework", s"/System/Library/Frameworks/${framework}.framework")) {
          println(frameworkPath)
          if (new File(frameworkPath).exists()) {
            result = s"-I$frameworkPath/Versions/A/Headers -framework $framework"
          }
        }
        println(result)
        if (result == null) throw new Exception(s"Can't find framework $framework")
        result
      }).mkString(" ")
    }

    var libSystem = "MacOS"
    if (OS.isWindows) libSystem = "Windows"

    val libraryAppend = libraries.map(library => {
      var result: String = null
      for (libraryPath <- List(s"$java_macos_embedded_frameworks/$library")) {
        println(libraryPath)
        if (new File(libraryPath).exists()) {
          result = s"-I$libraryPath/include/$libSystem $libraryPath/lib/$libSystem/lib$library.a"
        }
      }
      println(result)
      if (result == null) throw new Exception(s"Can't find library $library")
      result
    }).mkString(" ")


    val cflagsAppend = cflagsList.mkString(" ")
    var command = s"g++ -fpermissive -Wint-to-pointer-cast -O3 types.cpp main.cpp $paths $frameworksAppend $libraryAppend $cflagsAppend"
    if (OS.isMac) {
      command += " -framework Cocoa -framework CoreAudio -F/Library/Frameworks -F$java_macos_embedded_frameworks"
      command += " -D_THREAD_SAFE -lm -liconv -Wl,-framework,OpenGL -Wl,-framework,ForceFeedback -lobjc -Wl,-framework,Cocoa -Wl,-framework,Carbon -Wl,-framework,IOKit -Wl,-framework,CoreAudio -Wl,-framework,AudioToolbox -Wl,-framework,AudioUnit"
    }
    if (OS.isWindows) {
      command += " -static-libstdc++ -static-libgcc -L. -lopengl32 -lshell32 -luser32 -lgdi32 -lwinmm -limm32 -lole32 -lkernel32 -lversion -lOleAut32 -lstdc++"
    }

    val outputExecutableFile = s"$outputPath/a.out"
    println(command)
    new File(outputExecutableFile).delete()
    if (redirectProcess(Runtime.getRuntime.exec(command, null, new File(outputPath))) == 0) {
      if (OS.isMac) {
        BuildMacOS.createAPP(s"$outputPath/test.app", "sampleapp", Files.toByteArray(new File(outputExecutableFile)), png512)
      }

      if (OS.isWindows) {
        redirectProcess(Runtime.getRuntime.exec(outputPath + "/a.exe", null, new File(outputPath)))
      } else {
        redirectProcess(Runtime.getRuntime.exec("./a.out", null, new File(outputPath)))
      }
    }
  }

  private def redirectProcess(p: Process): Int = {
    for (line <- Source.fromInputStream(p.getInputStream).getLines()) {
      println(line)
    }

    for (line <- Source.fromInputStream(p.getErrorStream).getLines()) {
      println(line)
    }

    p.waitFor()
  }
}
