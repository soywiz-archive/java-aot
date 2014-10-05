package util

import java.io.File
import java.nio.file.{Paths, Path}

import vfs.{MergedVfsNode, FileVfsNode, VfsNode}

class RuntimeProvider {
  private val cl = this.getClass.getClassLoader
  private val currentClassPath = this.getClass.getName.replace(".", "/") + ".class"
  println(currentClassPath)
  private val javaAotProjectDirectory = new File(cl.getResource(currentClassPath).getPath).getParentFile.getParentFile.getParentFile
  private val javaAotProjectPath = javaAotProjectDirectory.getAbsolutePath

  val java_runtime_classes_path = Paths.get(s"$javaAotProjectPath/../../out_runtime").normalize().toFile.getAbsolutePath
  val java_sample1_classes_path = Paths.get(s"$javaAotProjectPath/../../out_sample1").normalize().toFile.getAbsolutePath
  val cpp_classes_path = Paths.get(s"$javaAotProjectPath/../../out_cpp").normalize().toFile.getAbsolutePath

  val runtimeClassesVfs = new MergedVfsNode(List(new FileVfsNode(java_runtime_classes_path)))

  def getClassVfsNode(className:String): VfsNode = runtimeClassesVfs.access(className.replace('.', '/') + ".class")
}
