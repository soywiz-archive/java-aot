package target

object OS {
  private val OS = System.getProperty("os.name").toLowerCase
  lazy val tempDir = System.getProperty("java.io.tmpdir")
  lazy val fileSeparator = System.getProperty("file.separator")

  def isWindows = OS.indexOf("win") >= 0
  def isMac = OS.indexOf("mac") >= 0
  def isUnix = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0
  def isSolaris = OS.indexOf("sunos") >= 0
}
