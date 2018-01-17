package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.expr.{FieldAccessExpr => JPFieldAccessExpr}
import com.github.woooking.cosyn.util.OptionConverters._

class FieldAccessExpr(override val delegate: JPFieldAccessExpr) extends Expression[JPFieldAccessExpr] {
    val scope: Expression[_] = delegate.getScope
    val name: String = delegate.getName
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala.map(l => l.toList)
}

object FieldAccessExpr {
    def apply(delegate: JPFieldAccessExpr): FieldAccessExpr = new FieldAccessExpr(delegate)

    def unapply(arg: FieldAccessExpr): Option[(
        Expression[_],
            String,
            Option[List[Type]]
        )] = Some((
        arg.scope,
        arg.name,
        arg.typeArgs
    ))
}