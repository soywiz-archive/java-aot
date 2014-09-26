package target.base

import java.io.File
import java.nio.charset.Charset

import soot.SootClass
import target.cpp.build.BuildMacOS
import target.{RuntimeProvider, ProcessUtils, OS, FileBytes}

import scala.collection.mutable

class BaseCompiler(runtimeProvider:RuntimeProvider, mangler:BaseMangler) {
  private val utf8 = Charset.forName("UTF-8")
  private val file_separator = System.getProperty("file.separator")
  private val cl = this.getClass.getClassLoader

  def compile(outputPath:String, libraries:mutable.HashSet[String], frameworks:mutable.HashSet[String], cflagsList:mutable.ListBuffer[String], processedList:mutable.HashSet[SootClass], mainClass:String) = {

    var java_macos_embedded_frameworks = runtimeProvider.java_sample1_classes_path + "/frameworks/cpp"
    if (OS.isWindows) java_macos_embedded_frameworks = "^/+".r.replaceAllIn(java_macos_embedded_frameworks, "")

    val mainClassName = mangler.mangleClassName(mainClass)
    FileBytes.write(new File(s"$outputPath/main.cpp"), utf8, "#include \"" + mainClassName + ".h\" \n int main(int argc, char **argv) { printf(\"Start!\\n\"); " + mainClassName + "::main(new Array<java_lang_String*>((java_lang_String**)0, 0)); return 0; }")
    val paths = processedList.filter(_.getName != "java.lang.Object").map(item => mangler.mangleFullClassName(item.getName) + ".cpp").mkString(" ")
    //FileBytes.write(new File(s"$outputPath/build.bat"), utf8, "@g++ -fpermissive -Wint-to-pointer-cast -g -ggdb -gstabs -gpubnames types.cpp main.cpp " + paths)

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
    var command = s"g++ -fpermissive -Wint-to-pointer-cast -O2 types.cpp main.cpp $paths $frameworksAppend $libraryAppend $cflagsAppend"
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

    FileBytes.write(new File(s"$outputPath/build.sh"), utf8, command)

    val result = ProcessUtils.runAndRedirect(command, new File(outputPath)) == 0
    if (result) {
      if (OS.isMac) {
        val png512 = FileBytes.read(new File(runtimeProvider.java_runtime_classes_path + "/emptyicon.png"))
        BuildMacOS.createAPP(s"$outputPath/test.app", "sampleapp", FileBytes.read(new File(outputExecutableFile)), png512)
      }
    }

    (result, outputExecutableFile)
  }
}
