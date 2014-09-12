package old

/**
 * Created by soywiz on 12/09/2014.
 */
case class FieldAccessExpr(base: Expr, fieldName: String, fieldDesc: String = "") extends LValue
