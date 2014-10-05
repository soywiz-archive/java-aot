package target.context

/**
 * Created by soywiz on 5/10/14.
 */
class BaseClassContext(val clazz:SootClass) {
   val referencedClasses = new mutable.HashSet[SootClass]
 }
