package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.expr.{NullLiteralExpr => JPNullLiteralExpr}

class NullLiteralExpr(override val delegate: JPNullLiteralExpr) extends Expression[JPNullLiteralExpr]


object NullLiteralExpr {
    def apply(delegate: JPNullLiteralExpr): NullLiteralExpr = new NullLiteralExpr(delegate)

    def unapply(arg: NullLiteralExpr): Boolean = true
}
