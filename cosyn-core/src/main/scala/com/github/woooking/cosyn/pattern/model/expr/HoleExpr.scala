package com.github.woooking.cosyn.pattern.model.expr

case class HoleExpr(id: Int, var fill: Option[Expression]) extends Expression with NameOrHole {
    override def toString: String = fill match {
        case None => "<HOLE>"
        case Some(value) => value.toString
    }
}

object HoleExpr {
    private var nextId = 0

    def apply(): HoleExpr = {
        val hole = new HoleExpr(nextId, None)
        nextId += 1
        hole
    }
}
