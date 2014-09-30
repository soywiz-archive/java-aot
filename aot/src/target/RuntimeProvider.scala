package target

import java.io.File

class RuntimeProvider {
  private val cl = this.getClass.getClassLoader
  private val javaAotProjectDirectory = new File(cl.getResource("target/SootUtils.class").getPath).getParentFile.getParentFile.getParentFile
  private val javaAotProjectPath = javaAotProjectDirectory.getAbsolutePath

  val java_runtime_classes_path = s"$javaAotProjectPath/../../out_runtime"
  val java_sample1_classes_path = s"$javaAotProjectPath/../../out_sample1"

  def getClassPath(className:String): String = {
    return java_runtime_classes_path + "/" + className.replace('.', '/') + ".class"
  }
}
