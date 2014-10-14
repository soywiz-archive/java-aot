package util

import java.io.File

import soot.{SootClass, SootMethod, Scene}
import soot.options.Options
import soot.tagkit.{AnnotationElem, AnnotationStringElem, Tag, VisibilityAnnotationTag}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object SootUtils {
  def init(classpath:List[String]): Unit = {
    soot.G.reset
    Options.v.set_output_format(Options.output_format_jimple)
    //Options.v.set_output_format(Options.output_format_grimple)
    Options.v.set_include_all(true)
    Options.v.set_print_tags_in_output(true)

    Options.v.set_allow_phantom_refs(false)
    //Options.v.set_allow_phantom_refs(true)

    Options.v.set_keep_line_number(true)

    val file_separator = OS.fileSeparator

    println(s"file_separator: $file_separator ... PathSeparator: ${File.pathSeparator}")

    Options.v.set_soot_classpath(classpath.mkString(File.pathSeparator))

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

  def getSuperClassOrNull(clazz:SootClass): SootClass = if (clazz.hasSuperclass) clazz.getSuperclass else null

  def classAncestors(clazz:SootClass): List[SootClass] = {
    val buffer = new ListBuffer[SootClass]
    var tclazz = getSuperClassOrNull(clazz)
    while (tclazz != null) {
      buffer.append(tclazz)
      tclazz = getSuperClassOrNull(tclazz)
    }
    buffer.toList
  }
  
  def hasMethod(clazz:SootClass, name:String, parameterTypes:java.util.List[_]):Boolean = {
    try {
      clazz.getMethod(name, parameterTypes) != null
    } catch {
      case _:Throwable => false
    }
  }

  def hasMethod(clazz:SootClass, method:SootMethod):Boolean = hasMethod(clazz, method.getName, method.getParameterTypes)

  def getAllDirectInterfaces(clazz:SootClass): List[SootClass] = {
    if (clazz.getInterfaceCount == 0) {
      List()
    } else {
      val clazzInterfaces = clazz.getInterfaces.asScala.toList
      clazzInterfaces.flatMap(clazzInterfaces ::: getAllDirectInterfaces(_))
    }
  }

  def getAllDirectAndIndirectInterfaces(clazz:SootClass): List[SootClass] = {
    if (clazz.getInterfaceCount == 0) {
      List()
    } else {
      val clazzInterfaces = clazz.getInterfaces.asScala.toList
      if (clazz.hasSuperclass) {
        getAllDirectAndIndirectInterfaces(clazz.getSuperclass) ::: clazzInterfaces.flatMap(clazzInterfaces ::: getAllDirectAndIndirectInterfaces(_))
      } else {
        clazzInterfaces.flatMap(clazzInterfaces ::: getAllDirectAndIndirectInterfaces(_))
      }
    }
  }

  def isMethodOverriding(method:SootMethod): Boolean = {
    val name = method.getName
    val parameterTypes = method.getParameterTypes
    val ancestors = classAncestors(method.getDeclaringClass)
    val abstractOverride = ancestors.filter(_.isAbstract).flatMap(getAllDirectAndIndirectInterfaces).exists(hasMethod(_, method))
    val normalOverride = ancestors.exists(hasMethod(_, name, parameterTypes))
    /*
    var methodString = method.toString()
    if (methodString == "<jflash.display.DisplayObject: void update(int)>") {
      println(method + " : " + abstractOverride + " : " + normalOverride)
      println(parameterTypes)
      println(ancestors)
    }
    if (abstractOverride) {
      //println("abstractOverride:" + method)
    }
    */
    abstractOverride || normalOverride
  }
}
