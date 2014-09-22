package output

import java.io.File

import soot.Scene
import soot.options.Options
import soot.tagkit.{AnnotationElem, AnnotationStringElem, Tag, VisibilityAnnotationTag}

import scala.collection.JavaConverters._

object SootUtils {
  def init(): Unit = {
    soot.G.reset
    Options.v.set_output_format(Options.output_format_jimple)
    Options.v.set_include_all(true)
    Options.v.set_print_tags_in_output(true)

    Options.v.set_allow_phantom_refs(false)
    //Options.v.set_allow_phantom_refs(true)

    Options.v.set_keep_line_number(true)

    val cl = this.getClass.getClassLoader
    val file_separator = OS.fileSeparator

    println(s"file_separator: $file_separator")

    val java_runtime_classes_path = "types\\.cpp$".r.replaceAllIn(cl.getResource("types.cpp").getPath, "/../../out/production/java_runtime")

    println(s"java_runtime_classes_path: $java_runtime_classes_path")

    Options.v.set_soot_classpath(List(java_runtime_classes_path).mkString(File.pathSeparator))

    Options.v.setPhaseOption("jb.dae", "enabled:false")
    Options.v.setPhaseOption("jb.uce", "enabled:false")
    Options.v.setPhaseOption("jap.npc", "enabled:true")
    Options.v.setPhaseOption("jap.abc", "enabled:true")
    Options.v.setPhaseOption("jop", "enabled:true")
    Options.v.setPhaseOption("jop.cse", "enabled:false")
    Options.v.setPhaseOption("jop.bcm", "enabled:false")
    Options.v.setPhaseOption("jop.lcm", "enabled:false")
    Options.v.setPhaseOption("jop.cp", "enabled:false")
    Options.v.setPhaseOption("jop.cpf", "enabled:false")
    Options.v.setPhaseOption("jop.cbf", "enabled:false")
    Options.v.setPhaseOption("jop.dae", "enabled:false")
    Options.v.setPhaseOption("jop.nce", "enabled:false")
    Options.v.setPhaseOption("jop.uce1", "enabled:false")
    Options.v.setPhaseOption("jop.ubf1", "enabled:false")
    Options.v.setPhaseOption("jop.uce2", "enabled:false")
    Options.v.setPhaseOption("jop.ubf2", "enabled:false")
    Options.v.setPhaseOption("jop.ule", "enabled:false")
    Scene.v.loadNecessaryClasses()
  }

  def getTag(tags: Iterable[Tag], clazz:String, name:String): Object = {
    for (tag <- tags) {
      tag match {
        case at:VisibilityAnnotationTag =>
          for (annotation <- at.getAnnotations.asScala) {
            if (annotation.getType == clazz) {
              //println(annotation.getName)
              //println(annotation.getNumElems)
              //println(annotation.getType)
              //println(annotation.getInfo)
              for (n <- 0 to annotation.getNumElems - 1) {
                val el = annotation.getElemAt(n)
                if (el.getName == name) {
                  def parseAnnotationElement(el:AnnotationElem):Object = {
                    el match {
                      case e: AnnotationStringElem => e.getValue
                      case _ => null
                    }
                    //println("::" + el.getName + " : " + el.getKind + " : " + el.toString)
                  }

                  return parseAnnotationElement(el)
                }
              }
            }
          }
        case _ =>
      }
      //println(tag.getName)
      //soot.tagkit.VisibilityAnnotationTag
      //println(tag.getValue)
    }
    null
  }
}
