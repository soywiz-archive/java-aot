package target.base

import java.io.File
import java.nio.charset.Charset

import soot.{Scene, SootClass}
import target.{RuntimeProvider, ProcessUtils, FileBytes, OS}

import scala.collection.mutable

abstract class BaseClassTreeGenerator(runtimeProvider:RuntimeProvider, mangler:BaseMangler, compiler:BaseCompiler, runner:BaseRunner) {
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

  def createClassGenerator(item:SootClass):BaseClassGenerator

  def run(mainClass:String) = {
    enqueue(mainClass)
    val utf8 = Charset.forName("UTF-8")
    //val outputPath = OS.tempDir
    val outputPath = runtimeProvider.cpp_classes_path

    val cl = this.getClass.getClassLoader
    val file_separator = System.getProperty("file.separator")

    val java_runtime_classes_path = runtimeProvider.java_runtime_classes_path

    try { new File(outputPath).mkdirs() } finally {}
    for (file <- List("types.cpp", "types.h", "gl.cpp")) {
      FileBytes.write(new File(s"$outputPath/$file"), FileBytes.read(new File(s"$java_runtime_classes_path/$file")))
    }

    val frameworks = new mutable.HashSet[String]
    val libraries = new mutable.HashSet[String]
    val cflagsList = new mutable.ListBuffer[String]
    val staticConstructors = new mutable.ListBuffer[StaticConstructorResult]

    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      println("Processing class: " + item.getName)
      val result = createClassGenerator(item).doClass()


      if (result.staticConstructor != null) staticConstructors.append(result.staticConstructor)
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

    val (result, executableOutputPath) = compiler.compile(runtimeProvider, outputPath, libraries, frameworks, cflagsList, processedList, mainClass, staticConstructors)
    if (result) {
      runner.run(outputPath, executableOutputPath)
    }
  }
}
