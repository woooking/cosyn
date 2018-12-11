package com.github.woooking.cosyn.pattern.model.expr
import com.github.woooking.cosyn.pattern.model.Node

sealed trait LiteralExpr extends Expression {
    override def children: Seq[Node] = Seq.empty
}

final case class BooleanLiteral(value: Boolean) extends LiteralExpr

final case class ByteLiteral(value: Byte) extends LiteralExpr

final case class ShortLiteral(value: Short) extends LiteralExpr

final case class IntLiteral(value: Int) extends LiteralExpr {
    override def toString: String = value.toString
}

final case class LongLiteral(value: Long) extends LiteralExpr

final case class FloatLiteral(value: Float) extends LiteralExpr

final case class DoubleLiteral(value: Double) extends LiteralExpr

final case class CharLiteral(value: Char) extends LiteralExpr

final case class StringLiteral(value: String) extends LiteralExpr {
    override def toString: String = s"""\"$value\""""
}
