package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.expr.{LambdaExpr => JPLambdaExpr}
import com.github.woooking.cosyn.javaparser.stmt.Statement

import scala.collection.JavaConverters._

class LambdaExpr(override val delegate: JPLambdaExpr) extends Expression[JPLambdaExpr] {
    val isEnclosingParams: Boolean = delegate.isEnclosingParameters
    val args: List[Parameter] = delegate.getParameters.asScala.toList
    val body: Statement = delegate.getBody
}

object LambdaExpr {
    def apply(delegate: JPLambdaExpr): LambdaExpr = new LambdaExpr(delegate)

    def unapply(arg: LambdaExpr): Option[(
        Boolean,
            List[Parameter],
            Statement
        )] = Some((
        arg.isEnclosingParams,
        arg.args,
        arg.body
    ))
}