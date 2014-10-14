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

  val project_root = Paths.get(s"$javaAotProjectPath/../..").normalize().toFile.getAbsolutePath
  val java_runtime_classes_path = s"$project_root/out_runtime"
  //val java_sample1_classes_path = s"$project_root/out_sample1"

  var runtimeClassesVfs = new MergedVfsNode(List(new FileVfsNode(java_runtime_classes_path)))
  var classpaths = List(java_runtime_classes_path)

  def setClassPaths(paths:List[String]): Unit = {
    classpaths = paths
    runtimeClassesVfs = new MergedVfsNode(List(new FileVfsNode(java_runtime_classes_path)) ::: paths.map(new FileVfsNode(_)))
  }

  def getClassVfsNode(className:String): VfsNode = runtimeClassesVfs.access(className.replace('.', '/') + ".class")
}
