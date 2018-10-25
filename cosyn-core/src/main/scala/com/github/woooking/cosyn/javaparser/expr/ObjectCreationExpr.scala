package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.`type`.{ClassOrInterfaceType, Type}
import com.github.javaparser.ast.expr.{ObjectCreationExpr => JPObjectCreationExpr}
import com.github.woooking.cosyn.util.OptionConverters._
import cats.instances.option._
import cats.instances.list._
import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.javaparser.body.BodyDeclaration

import scala.collection.JavaConverters._

class ObjectCreationExpr(override val delegate: JPObjectCreationExpr) extends Expression[JPObjectCreationExpr] {
    val scope: Option[Expression[_ <: Node]] = delegate.getScope.asScala
    val ty: ClassOrInterfaceType = delegate.getType
    val typeArgs: Option[List[Type]] = delegate.getTypeArguments.asScala
    val args: List[Expression[_ <: Node]] = delegate.getArguments.asScala.toList
    val anonymousClassBody: Option[List[BodyDeclaration[_ <: Node]]] = delegate.getAnonymousClassBody.asScala.map(l => l.asScala.map(d => BodyDeclaration(d)).toList)
}

object ObjectCreationExpr {
    def apply(delegate: JPObjectCreationExpr): ObjectCreationExpr = new ObjectCreationExpr(delegate)

    def unapply(arg: ObjectCreationExpr): Option[(
        Option[Expression[_ <: Node]],
            ClassOrInterfaceType,
            Option[List[Type]],
            List[Expression[_ <: Node]],
            Option[List[BodyDeclaration[_ <: Node]]]
        )] = Some((
        arg.scope,
        arg.ty,
        arg.typeArgs,
        arg.args,
        arg.anonymousClassBody
    ))
}

//    def unapply(arg: ObjectCreationExpr): Option[(
//        Option[Expression],
//            ClassOrInterfaceType,
//            Option[NodeList[Type]],
//            NodeList[Expression],
//            Option[NodeList[BodyDeclaration[_ <: BodyDeclaration[_]]]]
//        )] = Some((
//        arg.getScope.asScala,
//        arg.getType,
//        arg.getTypeArguments.asScala,
//        arg.getArguments,
//        arg.getAnonymousClassBody.asScala,
//    ))

