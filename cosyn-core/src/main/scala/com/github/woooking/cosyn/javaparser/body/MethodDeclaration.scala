package com.github.woooking.cosyn.javaparser.body

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.{ReferenceType, Type, TypeParameter}
import com.github.javaparser.ast.body.{Parameter, ReceiverParameter, MethodDeclaration => JPMethodDeclaration}
import com.github.woooking.cosyn.javaparser.stmt.BlockStmt
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class MethodDeclaration(override val delegate: JPMethodDeclaration) extends BodyDeclaration[JPMethodDeclaration] {
    val name: String = delegate.getName

    val ty: Type = delegate.getType

    val typeParams: List[TypeParameter] = delegate.getTypeParameters.asScala.toList

    val params: List[Parameter] = delegate.getParameters.asScala.toList

    val receiveParam: Option[ReceiverParameter] = delegate.getReceiverParameter.asScala

    val exceptions: List[ReferenceType] = delegate.getThrownExceptions.asScala.toList

    val body: Option[BlockStmt] = delegate.getBody.asScala.map(s => BlockStmt(s))

    def signature: String = delegate.getSignature.asString()
}

object MethodDeclaration {
    def apply(delegate: JPMethodDeclaration): MethodDeclaration = new MethodDeclaration(delegate)

    def unapply(arg: MethodDeclaration): Option[(
        String,
            Type,
            List[TypeParameter],
            List[Parameter],
            Option[ReceiverParameter],
            List[ReferenceType],
            Option[BlockStmt]
        )] = Some((
        arg.name,
        arg.ty,
        arg.typeParams,
        arg.params,
        arg.receiveParam,
        arg.exceptions,
        arg.body
    ))
}


