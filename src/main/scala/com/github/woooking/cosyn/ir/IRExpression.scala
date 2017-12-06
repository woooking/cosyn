package com.github.woooking.cosyn.ir

import com.github.woooking.cosyn.cfg.CFG

sealed trait IRExpression extends NodeResult

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

trait IRVariable extends IRExpression

case object IRUndef extends IRVariable

case class IRTemp(id: Int) extends IRVariable

private case class IRBoolean(value: Boolean) extends IRExpression

case class IRChar(value: Char) extends IRExpression
