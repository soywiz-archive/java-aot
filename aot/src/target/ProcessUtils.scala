package target

import java.io.{File, IOException, BufferedReader, InputStreamReader}

import scala.io.Source
import scala.sys.process._
import scala.sys.process.ProcessLogger

object ProcessUtils {
  def runAndRedirect(command:String, currentDir:File): Int = {
    val logger = ProcessLogger(
      (o: String) => println("out " + o),
      (e: String) => println("err " + e))

    val pb = Process(command, currentDir)

    pb ! logger
  }

  /*
  def redirectProcess(p: Process): Unit = {
    try {
      val br1 = new BufferedReader(new InputStreamReader(p.getInputStream))
      val br2 = new BufferedReader(new InputStreamReader(p.getErrorStream))
      var line1:String=null
      var line2:String=null
      do {
        line1 = br1.readLine()
        if (line1 != null) println(line1)
        line2 = br2.readLine()
        if (line2 != null) println(line2)
      } while ((line1 != null) || (line2 != null))
    } catch {
      case ioe:IOException => ioe.printStackTrace()
    }

    /*
    for (line <- Source.fromInputStream(p.getErrorStream).getLines()) {
      println(line)
    }
    */

    p.waitFor()
  }
  */
}
