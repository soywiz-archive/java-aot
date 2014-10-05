package util

import java.io.File

import vfs.{MergedVfsNode, FileVfsNode, VfsNode}

class RuntimeProvider {
  private val cl = this.getClass.getClassLoader
  private val javaAotProjectDirectory = new File(cl.getResource("target/SootUtils.class").getPath).getParentFile.getParentFile.getParentFile
  private val javaAotProjectPath = javaAotProjectDirectory.getAbsolutePath

  val java_runtime_classes_path = s"$javaAotProjectPath/../../out_runtime"
  val java_sample1_classes_path = s"$javaAotProjectPath/../../out_sample1"
  val cpp_classes_path = s"$javaAotProjectPath/../../out_cpp"

  val runtimeClassesVfs = new MergedVfsNode(List(new FileVfsNode(java_runtime_classes_path)))

  def getClassVfsNode(className:String): VfsNode = runtimeClassesVfs.access(className.replace('.', '/') + ".class")
}
