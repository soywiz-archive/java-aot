package old

import org.objectweb.asm.Type

/**
 * Created by soywiz on 12/09/2014.
 */
case class CastExpr(expr: Expr, from: Type, to: Type) extends Expr
