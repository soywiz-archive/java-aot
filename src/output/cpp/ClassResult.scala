package output.cpp

import soot.SootClass

case class ClassResult(clazz:SootClass, methods:List[MethodResult], declaration:String, definition:String, referencedClasses:List[SootClass])
