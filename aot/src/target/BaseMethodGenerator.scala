package target

import _root_.util.SootUtils
import soot._
import target.result.MethodResult

import scala.collection.JavaConverters._

abstract class BaseMethodGenerator(method: SootMethod, mangler: BaseMangler) {
  val bodyGenerator:TargetBase
  val signatureGenerator:BaseMethodSignatureGenerator

  def doMethod(): MethodResult = {
    bodyGenerator.calculateSignatureDependencies()

    def getBody:String = {
      if (method.isAbstract || method.isNative) {
        SootUtils.getTag(method.getTags.asScala, "Llibcore/CPPMethod;", "value").asInstanceOf[String]
      } else {
        signatureGenerator.generateBody(bodyGenerator.doMethodBody())
      }
    }

    MethodResult(method, signatureGenerator.generateHeader(), getBody, bodyGenerator.getReferencedClasses)
  }
}

