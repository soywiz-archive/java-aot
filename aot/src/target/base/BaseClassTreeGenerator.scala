package target.base

import java.io.File
import java.nio.charset.Charset

import soot.{Scene, SootClass}
import target.{ProcessUtils, FileBytes, OS}
import target.as3.As3ClassGenerator
import target.cpp.build.BuildMacOS

import scala.collection.mutable
import scala.io.Source

abstract class BaseClassTreeGenerator(mangler:BaseMangler, compiler:BaseCompiler, runner:BaseRunner) {
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
    val utf8 = Charset.forName("UTF-8")
    val outputPath = System.getProperty("java.io.tmpdir")

    val cl = this.getClass.getClassLoader
    val file_separator = System.getProperty("file.separator")

    FileBytes.write(new File(s"$outputPath/types.cpp"), FileBytes.read(new File(cl.getResource("types.cpp").getPath)))
    FileBytes.write(new File(s"$outputPath/types.h"), FileBytes.read(new File(cl.getResource("types.h").getPath)))

    val frameworks = new mutable.HashSet[String]
    val libraries = new mutable.HashSet[String]
    val cflagsList = new mutable.ListBuffer[String]

    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      println("Processing class: " + item.getName)
      val result = new As3ClassGenerator(item).doClass()

      if (result.nativeFramework != null) frameworks.add(result.nativeFramework)
      if (result.nativeLibrary != null) libraries.add(result.nativeLibrary)
      if (result.cflags != null) cflagsList.append(result.cflags)

      FileBytes.write(new File(outputPath + "/" + mangler.mangleFullClassName(item.getName) + ".h"), utf8, result.declaration)
      FileBytes.write(new File(outputPath + "/" + mangler.mangleFullClassName(item.getName) + ".cpp"), utf8, result.definition)

      //println(result.definition)
      //println(result.declaration)
      for (referencedClass <- result.referencedClasses) {
        enqueue(referencedClass)
        //println(s"  Referencing: ${referencedClass.getName}");
      }
    }
    println("Processed classes: " + processedList.size)

    val (result, executableOutputPath) = compiler.compile(outputPath, libraries, frameworks, cflagsList, processedList)
    if (result) {
      runner.run(outputPath, executableOutputPath)
    }
  }
}
