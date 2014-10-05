package util

import java.io.File

class RuntimeProvider {
  private val cl = this.getClass.getClassLoader
  private val javaAotProjectDirectory = new File(cl.getResource("target/SootUtils.class").getPath).getParentFile.getParentFile.getParentFile
  private val javaAotProjectPath = javaAotProjectDirectory.getAbsolutePath

  val java_runtime_classes_path = s"$javaAotProjectPath/../../out_runtime"
  val java_sample1_classes_path = s"$javaAotProjectPath/../../out_sample1"
  val cpp_classes_path = s"$javaAotProjectPath/../../out_cpp"

  def getClassPath(className:String): String = java_runtime_classes_path + "/" + className.replace('.', '/') + ".class"
}
