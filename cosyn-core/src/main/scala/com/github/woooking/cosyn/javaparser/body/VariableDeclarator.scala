package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.Node
import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.body.{VariableDeclarator => JPVariableDeclarator}
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

class VariableDeclarator(override val delegate: JPVariableDeclarator) extends NodeDelegate[JPVariableDeclarator] {
    val name: String = delegate.getName.asString()
    val ty: Type = delegate.getType
    val initializer: Option[Expression[_<: Node]] = delegate.getInitializer.asScala.map(e => Expression(e))
}

object VariableDeclarator {
    def apply(delegate: JPVariableDeclarator): VariableDeclarator = new VariableDeclarator(delegate)

    def unapply(arg: VariableDeclarator): Option[(
        String,
            Type,
            Option[Expression[_<: Node]]
        )] = Some((
        arg.name,
        arg.ty,
        arg.initializer
    ))
}