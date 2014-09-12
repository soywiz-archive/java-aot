package old

import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.{Opcodes, Type}

import scala.collection.mutable.ListBuffer

/**
 * Created by soywiz on 12/09/2014.
 */
object CppGenerator {
  def descToCppType(jtype:Type):String = {
    jtype.getSort match {
      case Type.VOID => "void"
      case Type.CHAR => "u16"
      case Type.SHORT => "s16"
      case Type.INT => "s32"
      case Type.LONG => "s64"
      case Type.FLOAT => "f32"
      case Type.DOUBLE => "f64"
      case Type.OBJECT => jtype.getClassName + "*"
      case _ => "Unhandled_" + jtype.getDescriptor
    }
  }

  def nameToCppName(name:String):String = {
    return name.replace('<', '_').replace('>', '_').replace('$', '_')
  }

  def generateMethodSignature(className:String, method:MethodNode, node:Node, variables:ListBuffer[Var], arguments:ListBuffer[Var]): String = {
    val isStatic = ((method.access & Opcodes.ACC_STATIC) != 0)
    val methodType = Type.getMethodType(method.desc)
    val static = if (isStatic) "static " else ""
    val head = descToCppType(methodType.getReturnType) + " " + nameToCppName(method.name)
    val args = "(" + (for (argument <- arguments.drop(if (isStatic) 0 else 1)) yield descToCppType(argument.kind) + " " + argument.name).mkString(", ") + ");"
    s"public: ${static}${head}${args}"
  }

  def generateMethod(className:String, method:MethodNode, node:Node, variables:ListBuffer[Var], arguments:ListBuffer[Var]): String = {
    val isStatic = ((method.access & Opcodes.ACC_STATIC) != 0)
    val methodType = Type.getMethodType(method.desc)
    val head = descToCppType(methodType.getReturnType) + " " + className + "::" + nameToCppName(method.name)
    val args = "(" + (for (argument <- arguments.drop(if (isStatic) 0 else 1)) yield descToCppType(argument.kind) + " " + argument.name).mkString(", ") + ") "
    val body = "{\n" + generateVariableDefinitions(variables) + "\n" + generateCode(node) + "}\n"
    s"${head}${args}${body}"
  }

  def generateVariableDefinitions(variables:ListBuffer[Var]): String = {
    var out = ""
    for (variable <- variables) out += descToCppType(variable.kind) + " " + variable.name + ";"
    out
  }

  def generatePrefix() = {
    "#include <stdio.h>\n" +
      "typedef int s32;\n" +
      "typedef long long int s64;\n"
  }

  def generateCode(node: Node): String = {
    node match {
      case expr: Expr =>
        expr match {
          case _return: VoidExpr => ""
          case varexpr: VarExpr => varexpr.variable.name
          case tempExpr: TempExpr => s"temp_${tempExpr.index}"
          case classref: TypeRefExpr => classref.name
          case arraya:ArrayAccessExpr => generateCode(arraya.expr) + "[" + generateCode(arraya.index) + "]"
          case check:CheckCastExpr => "((" + descToCppType(check.kind) + ")(" + generateCode(check.expr) + "))"
          case assignTemp:AssignTemp => "auto temp_" + assignTemp.index + " = " + generateCode(assignTemp.expr)
          case newArrayExpr:NewArrayExpr => "new " + descToCppType(newArrayExpr.kind) + "[" + generateCode(newArrayExpr.countExpr) + "]"
          case newExpr:NewExpr => "(new " + descToCppType(newExpr.kind) + "())"
          case binop:BinOp => "(" + generateCode(binop.left) + " " + binop.op + " " + generateCode(binop.right) + ")"
          case cast:CastExpr => "((" + descToCppType(cast.to) + ")(" + generateCode(cast.expr) + "))"
          case const: ConstExpr =>
            const.value match {
              case string: String => "\"" + const.value.toString + "\""
              case _ => const.value.toString
            }
          case fieldaccess: FieldAccessExpr =>
            fieldaccess.base match {
              case classref: TypeRefExpr =>
                s"${classref.name}::${fieldaccess.fieldName}"
              case _ =>
                generateCode(fieldaccess.base) + "->" + fieldaccess.fieldName
            }

          case methodCall: MethodCallExpr =>
            val methodExpr = methodCall.thisExpr match {
              case _:TypeRefExpr => generateCode(methodCall.thisExpr) + "::"
              case _ => generateCode(methodCall.thisExpr) + "->"
            }
            val methodName = nameToCppName(methodCall.methodName)
            val argsList = (for (arg <- methodCall.args) yield generateCode(arg)).mkString(", ")
            s"${methodExpr}${methodName}(${argsList})"

          case _ => node.toString
        }
      case stm: Stm =>
        stm match {
          case label:LabelStm => "label_" + label.label + ":;\n"
          case jump:JumpIfStm => "if (" + generateCode(jump.expr) + ") goto label_" + jump.label + ";\n"
          case jump:JumpStm => "goto label_" + jump.label + ";\n"
          case list: StmList => (for (node2 <- list.nodes) yield generateCode(node2)).mkString("")
          case assignStm: AssignStm => generateCode(assignStm.lvalue) + " = " + generateCode(assignStm.expr) + ";\n"
          case exprStm: ExprStm => generateCode(exprStm.expr) + ";\n"
          case _return: ReturnStm => s"return ${generateCode(_return.expr)};\n"
          //case linen: LineNumberStm => s"// line ${linen.line}\n"
          case linen: LineNumberStm => ""
          case _ => node.toString + "\n"
        }
      case _ => node.toString + "\n"
    }
  }
}
