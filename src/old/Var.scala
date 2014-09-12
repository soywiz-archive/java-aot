package old

import org.objectweb.asm.Type

/**
 * Created by soywiz on 12/09/2014.
 */
class Var(val kind:Type, val name:String, val index:Int, val start:Int, val end:Int, val isArgument:Boolean) {}
