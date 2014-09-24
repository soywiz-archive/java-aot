package target.as3

import soot.SootClass

case class ClassResult(clazz:SootClass, methods:List[MethodResult], declaration:String, definition:String, referencedClasses:List[SootClass], nativeFramework:String, nativeLibrary:String, cflags:String)
