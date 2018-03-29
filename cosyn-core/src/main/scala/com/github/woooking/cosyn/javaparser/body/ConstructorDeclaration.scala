package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.`type`.{ReferenceType, TypeParameter}
import com.github.javaparser.ast.body.{Parameter, ReceiverParameter, ConstructorDeclaration => JPConstructorDeclaration}
import com.github.woooking.cosyn.javaparser.stmt.BlockStmt
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class ConstructorDeclaration(override val delegate: JPConstructorDeclaration) extends BodyDeclaration[JPConstructorDeclaration] {
    val name: String = delegate.getNameAsString

    val typeParams: List[TypeParameter] = delegate.getTypeParameters.asScala.toList

    val params: List[Parameter] = delegate.getParameters.asScala.toList

    val receiveParam: Option[ReceiverParameter] = delegate.getReceiverParameter.asScala

    val exceptions: List[ReferenceType] = delegate.getThrownExceptions.asScala.toList

    val body: BlockStmt = BlockStmt(delegate.getBody)

    def signature: String = delegate.getSignature.asString()
}

object ConstructorDeclaration {
    def apply(delegate: JPConstructorDeclaration): ConstructorDeclaration = new ConstructorDeclaration(delegate)

    def unapply(arg: ConstructorDeclaration): Option[(
        String,
            List[TypeParameter],
            List[Parameter],
            Option[ReceiverParameter],
            List[ReferenceType],
            BlockStmt
        )] = Some((
        arg.name,
        arg.typeParams,
        arg.params,
        arg.receiveParam,
        arg.exceptions,
        arg.body
    ))
}