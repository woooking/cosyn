package com.github.woooking.cosyn.javaparser

import cats.Functor
import com.github.javaparser.ast.{Node, NodeList}
import com.github.javaparser.ast.expr.{SimpleName, Expression => JPExpression}
import com.github.javaparser.ast.body.{BodyDeclaration => JPBodyDeclaration}
import com.github.woooking.cosyn.javaparser.body.BodyDeclaration
import com.github.woooking.cosyn.javaparser.expr.Expression

import scala.collection.JavaConverters._

trait NodeDelegate[T] {
    val delegate: T

    implicit def jpExpr2expr(expr: JPExpression): Expression[_] = Expression(expr)

    implicit def jpBodyDecl2bodyDecl[A <: JPBodyDeclaration[_]](bodyDeclaration: JPBodyDeclaration[A]): BodyDeclaration[_] = BodyDeclaration(bodyDeclaration)

    implicit def jpSimpleName2string(simpleName: SimpleName): String = simpleName.asString()

    implicit def jpNodeList2list[A <: Node](nodeList: NodeList[A]): List[A] = nodeList.asScala.toList

    implicit def liftConversion[F[_], A, B](x: F[A])(implicit f: A => B, functor: Functor[F]): F[B] = functor.fmap(x)(f)

}

