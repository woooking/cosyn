package com.github.woooking.cosyn.pattern.model.expr

trait LiteralExpr extends Expression

sealed case class BooleanLiteral(value: Boolean) extends LiteralExpr

sealed case class ByteLiteral(value: Byte) extends LiteralExpr

sealed case class ShortLiteral(value: Short) extends LiteralExpr

sealed case class IntLiteral(value: Int) extends LiteralExpr {
    override def toString: String = value.toString
}

sealed case class LongLiteral(value: Long) extends LiteralExpr

sealed case class FloatLiteral(value: Float) extends LiteralExpr

sealed case class DoubleLiteral(value: Double) extends LiteralExpr

sealed case class CharLiteral(value: Char) extends LiteralExpr

sealed case class StringLiteral(value: String) extends LiteralExpr {
    override def toString: String = s"""\"$value\""""
}
