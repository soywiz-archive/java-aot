package target.result

/**
 * Created by soywiz on 5/10/14.
 */
case class MethodResult(method:SootMethod, declaration:String, definition:String, referencedClasses:List[SootClass])
