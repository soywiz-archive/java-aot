package target.base

import soot._
import soot.jimple._
import target.SootUtils

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class BaseMethodGenerator(method: SootMethod, mangler: BaseMangler) {
  val bodyGenerator:BaseMethodBodyGenerator
  val signatureGenerator:BaseMethodSignatureGenerator

  def doMethod(): MethodResult = {
    bodyGenerator.calculateSignatureDependencies()

    def getBody:String = {
      if (method.isAbstract || method.isNative) {
        SootUtils.getTag(method.getTags.asScala, "Llibcore/CPPMethod;", "value").asInstanceOf[String]
      } else {
        signatureGenerator.generateBody(bodyGenerator.generateBody())
      }
    }

    MethodResult(method, signatureGenerator.generateHeader(), getBody, bodyGenerator.getReferencedClasses)
  }
}

