package old

import org.objectweb.asm.Label

/**
 * Created by soywiz on 12/09/2014.
 */
case class JumpIfStm(expr:Expr, label:Label) extends Stm
