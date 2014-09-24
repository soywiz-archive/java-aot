package target.base

import soot.{SootClass, SootMethod}

case class MethodResult(method:SootMethod, declaration:String, definition:String, referencedClasses:List[SootClass])
