package com.github.woooking.cosyn.pattern.model.expr

case class VariableDeclaration(ty: String, name: String, init: Option[Expression]) extends Expression

object VariableDeclaration {
    def apply(ty: String, name: String): VariableDeclaration = new VariableDeclaration(ty, name, None)

    def apply(ty: String, name: String, init: Expression): VariableDeclaration = new VariableDeclaration(ty, name, Some(init))
}

