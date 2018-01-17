package com.github.woooking.cosyn.ir

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.ir.statements.IRAbstractStatement

import scala.collection.mutable

sealed trait IRExpression extends NodeResult

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

trait IRVariable extends IRExpression {
    val uses: mutable.Set[IRAbstractStatement] = mutable.Set()
}

case object IRUndef extends IRVariable

case class IRExtern(name: String) extends IRVariable {
    override def toString: String = name
}

case class IRArg(name: String, ty: Type) extends IRVariable {
    override def toString: String = name
}

sealed case class IRBoolean(value: Boolean) extends IRExpression {
    override def toString: String = s"$value"
}

case class IRChar(value: Char) extends IRExpression

case class IRString(value: String) extends IRExpression {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int) extends IRExpression {
    override def toString: String = s"$value"
}

case object IRNull extends IRVariable {
    override def toString: String = "null"
}

case object IRThis extends IRVariable

case class IRTypeObject(ty: Type) extends IRVariable {
    override def toString: String = ty.asString()
}

