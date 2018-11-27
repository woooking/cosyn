package com.github.woooking.cosyn.pattern.model.expr

case class HoleExpr(var fill: Option[Expression]) extends Expression with NameOrHole

object HoleExpr {
    def apply(): HoleExpr = new HoleExpr(None)
}
