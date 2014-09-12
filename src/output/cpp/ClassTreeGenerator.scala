package output.cpp

import soot.{Scene, SootClass}

import scala.collection.mutable

class ClassTreeGenerator {
  private val processedList = new mutable.HashSet[SootClass]
  private val toProcessList = new mutable.Queue[SootClass]

  def enqueue(className:String):Unit = {
    enqueue(Scene.v.loadClassAndSupport(className))
  }
  
  def enqueue(clazz:SootClass):Unit = {
    if (!processedList.contains(clazz)) toProcessList.enqueue(clazz)
  }

  def run() = {
    while (toProcessList.length > 0) {
      val item = toProcessList.dequeue()
      processedList.add(item)
      println("Processing class: " + item.getName)
      val result = new ClassGenerator(item).doClass()
      for (referencedClass <- result.referencedClasses) enqueue(referencedClass)
    }
    println("Processed classes: " + processedList.size)
  }
}
