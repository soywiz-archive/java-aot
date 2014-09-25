package target.base

import java.io.File

import target.{FileBytes, OS, ProcessUtils}

class BaseRunner {
  def run(outputPath:String, outputExecutableFile:String): Unit = {
    ProcessUtils.redirectProcess(Runtime.getRuntime.exec(outputExecutableFile, null, new File(outputPath)))
  }
}
