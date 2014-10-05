package util

import java.io.File

import vfs.{MergedVfsNode, FileVfsNode, VfsNode}

class RuntimeProvider {
  private val cl = this.getClass.getClassLoader
  private val currentClassPath = this.getClass.getName.replace(".", "/") + ".class"
  println(currentClassPath)
  private val javaAotProjectDirectory = new File(cl.getResource(currentClassPath).getPath).getParentFile.getParentFile.getParentFile
  private val javaAotProjectPath = javaAotProjectDirectory.getAbsolutePath

  val java_runtime_classes_path = s"$javaAotProjectPath/../../out_runtime"
  val java_sample1_classes_path = s"$javaAotProjectPath/../../out_sample1"
  val cpp_classes_path = s"$javaAotProjectPath/../../out_cpp"

  val runtimeClassesVfs = new MergedVfsNode(List(new FileVfsNode(java_runtime_classes_path)))

  def getClassVfsNode(className:String): VfsNode = runtimeClassesVfs.access(className.replace('.', '/') + ".class")
}
