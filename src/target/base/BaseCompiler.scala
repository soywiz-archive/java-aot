package target.base

import java.io.File
import java.nio.charset.Charset

import soot.SootClass
import target.cpp.build.BuildMacOS
import target.{ProcessUtils, OS, FileBytes}

import scala.collection.mutable

class BaseCompiler {
  private val utf8 = Charset.forName("UTF-8")
  private val file_separator = System.getProperty("file.separator")
  private val cl = this.getClass.getClassLoader

  def compile(outputPath:String, mangler:BaseMangler, libraries:mutable.HashSet[String], frameworks:mutable.HashSet[String], cflagsList:mutable.ListBuffer[String], processedList:mutable.HashSet[SootClass]): Tuple2[Boolean, String] = {
    var java_macos_embedded_frameworks = "types\\.cpp$".r.replaceAllIn(cl.getResource("types.cpp").getPath, "/frameworks".replace("/", file_separator))
    if (OS.isWindows) java_macos_embedded_frameworks = "^/+".r.replaceAllIn(java_macos_embedded_frameworks, "")

    FileBytes.write(new File(s"$outputPath/main.cpp"), utf8, "#include \"java_Simple1.h\" \n int main(int argc, char **argv) { printf(\"Start!\\n\"); java_Simple1::main(new Array<java_lang_String*>((java_lang_String**)0, 0)); return 0; }")
    val paths = processedList.filter(_.getName != "java.lang.Object").map(item => mangler.mangleFullClassName(item.getName) + ".cpp").mkString(" ")
    FileBytes.write(new File(s"$outputPath/build.bat"), utf8, "@g++ -fpermissive -Wint-to-pointer-cast -g -ggdb -gstabs -gpubnames types.cpp main.cpp " + paths)
    FileBytes.write(new File(s"$outputPath/build.sh"), utf8, "g++ -fpermissive -Wint-to-pointer-cast -O3 types.cpp main.cpp " + paths)

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

    val outputExecutableFile = if (OS.isWindows) {
      s"$outputPath/a.exe"
    } else {
      s"$outputPath/a.out"
    }
    println(command)
    new File(outputExecutableFile).delete()

    val result = ProcessUtils.redirectProcess(Runtime.getRuntime.exec(command, null, new File(outputPath))) == 0
    if (result) {
      val png512 = FileBytes.read(new File(cl.getResource("emptyicon.png").getPath))

      if (OS.isMac) {
        BuildMacOS.createAPP(s"$outputPath/test.app", "sampleapp", FileBytes.read(new File(outputExecutableFile)), png512)
      }
    }

    (result, outputExecutableFile)
  }
}