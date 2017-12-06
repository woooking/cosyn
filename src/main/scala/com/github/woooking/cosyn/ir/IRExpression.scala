package com.github.woooking.cosyn.ir

sealed trait IRExpression extends NodeResult

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

trait IRVariable extends IRExpression

case object IRUndef extends IRVariable

sealed case class IRBoolean(value: Boolean) extends IRExpression

case class IRChar(value: Char) extends IRExpression
