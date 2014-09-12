package old

/**
 * Created by soywiz on 12/09/2014.
 */
case class MethodCallExpr(className: String, methodName: String, methodType: String, thisExpr: Expr, args: Array[Expr]) extends Expr
