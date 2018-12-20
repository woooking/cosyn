package com.github.woooking.cosyn.code.model.expr

import com.github.woooking.cosyn.code.model.Node
import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}
import com.github.woooking.cosyn.util.CodeUtil.qualifiedClassName2Simple

case class StaticFieldAccessExpr(receiverType: BasicType, targetType: Type, name: NameOrHole) extends Expression {
    name.parent = this
    override def toString: String = s"${qualifiedClassName2Simple(receiverType.ty)}.$name"

    override def children: Seq[Node] = Seq(name)
}

object StaticFieldAccessExpr {
    def apply(receiverType: String, targetType: String, name: NameOrHole): StaticFieldAccessExpr =
        new StaticFieldAccessExpr(BasicType(receiverType), BasicType(targetType), name)
}



