package com.github.woooking.cosyn.pattern.model.expr

case class HoleExpr(var fill: Option[Expression]) extends Expression with NameOrHole {
    override def toString: String = fill match {
        case None => "<HOLE>"
        case Some(value) => value.toString
    }
}

object HoleExpr {
    def apply(): HoleExpr = new HoleExpr(None)
}
