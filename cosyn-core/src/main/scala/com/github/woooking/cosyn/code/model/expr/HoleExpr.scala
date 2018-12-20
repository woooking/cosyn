package com.github.woooking.cosyn.code.model.expr

import com.github.woooking.cosyn.code.model.Node

case class HoleExpr(id: Int, private var _fill: Option[Expression]) extends Expression with NameOrHole {
    def fill: Option[Expression] = _fill

    def fill_= (newFill: Expression): Unit = {
       _fill = Some(newFill)
        newFill.parent = this.parent
    }

    override def toString: String = fill match {
        case None => "<HOLE>"
        case Some(value) => value.toString
    }

    override def children: Seq[Node] = _fill.toSeq
}

object HoleExpr {
    private var nextId = 0

    def apply(): HoleExpr = {
        val hole = new HoleExpr(nextId, None)
        nextId += 1
        hole
    }
}
