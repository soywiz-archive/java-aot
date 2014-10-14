import target.{TargetAs3, TargetCpp}
import util.{ClassDependencyWalker, OS, RuntimeProvider, SootUtils}
import vfs.FileVfsNode

import scala.collection.mutable.ListBuffer

object Main extends App {
  val runtimeProvider = new RuntimeProvider()
  var classPathList = new ListBuffer[String]
  var entryPoint = ""
  var targetName = "as3"
  var verbose = false

  def showHelp(): Unit = {
    println("Usage: java-aot -target <target> -cp <path_to_classes> sample1.MainAs3")
    println("Available targets: as3, cpp")
    System.exit(-1)
  }

  def processArgs(args:Seq[String]): Unit = {
    args match {
      case List() =>
      case List("-h" | "--help", _*) =>
        showHelp()
      case List("-cp" | "--classpath", cp, _*) =>
        classPathList.append(cp)
        processArgs(args.drop(2))
      case List("-t" | "--target", _targetName, _*) =>
        targetName = _targetName
        processArgs(args.drop(2))
      case List("-v" | "--verbose", _*) =>
        verbose = true
        processArgs(args.drop(1))
      case List(_entryPoint:String, _*) =>
        entryPoint = _entryPoint
        processArgs(args.drop(1))
    }

    /*
    if (args.nonEmpty) {
      val head = args.head
      val rest = args.tail
      head match {
        case "-cp" | "--classpath" =>
          val cp = rest.head
          classPathList.append(cp)
          //println(cp)
          processArgs(rest.tail)
        case "-t" | "--target" =>
          targetName = rest.head
          processArgs(rest.tail)
        case "-v" | "--verbose" =>
          verbose = true
          processArgs(rest)
        case "-h" | "--help" =>
          showHelp()
        case _ =>
          entryPoint = head
          processArgs(rest)
          //println(head)
      }
    }
      */
  }

  var arguments = args.toList

  arguments = List("-cp", s"${runtimeProvider.project_root}/out_sample1", "sample1.MainAs3")

  if (arguments.isEmpty) showHelp()
  processArgs(arguments)
  println(arguments)

  System.out.println(System.getProperty("os.name").toLowerCase)
  System.out.println(s"OS current temporary directory is ${OS.tempDir}")

  SootUtils.init(runtimeProvider.classpaths)

  //val target = new TargetCpp()
  val target = targetName match {
    case "as3" => new TargetAs3()
    case "cpp" => new TargetCpp()
  }
  println(s"Targetting ... $targetName -> $target")
  print("Calculating dependency tree...")
  val dependencies = new ClassDependencyWalker(runtimeProvider).getRefClassesTree(entryPoint)

  println("" + dependencies.length + "...Ok")
  println(s"target:${target.targetName}")

  target.buildAndRun(dependencies, entryPoint, runtimeProvider, new FileVfsNode(s"${runtimeProvider.project_root}/out_${target.targetName}"))
}