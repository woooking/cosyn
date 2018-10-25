package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.body.{InitializerDeclaration => JPInitializerDeclaration}
import com.github.woooking.cosyn.javaparser.stmt.BlockStmt

class InitializerDeclaration(override val delegate: JPInitializerDeclaration) extends BodyDeclaration[JPInitializerDeclaration] {
    val isStatic: Boolean = delegate.isStatic

    val body: BlockStmt = BlockStmt(delegate.getBody)
}

object InitializerDeclaration {
    def apply(delegate: JPInitializerDeclaration): InitializerDeclaration = new InitializerDeclaration(delegate)

    def unapply(arg: InitializerDeclaration): Option[(Boolean, BlockStmt)] = Some((arg.isStatic, arg.body))
}