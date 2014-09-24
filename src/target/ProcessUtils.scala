package target

import scala.io.Source

object ProcessUtils {
  def redirectProcess(p: Process): Int = {
    for (line <- Source.fromInputStream(p.getInputStream).getLines()) {
      println(line)
    }

    for (line <- Source.fromInputStream(p.getErrorStream).getLines()) {
      println(line)
    }

    p.waitFor()
  }
}
