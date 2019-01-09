package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}
import com.github.woooking.cosyn.util.CodeUtil
import com.github.woooking.cosyn.util.CodeUtil.qualifiedClassName2Simple

sealed trait Node

sealed trait Statement extends Node {
    def generateCode(indent: String): String = s"$indent$this"
}

sealed case class BlockStmt(statements: Seq[Statement]) extends Statement {
    override def toString: String = statements.mkString("\n")

    override def generateCode(indent: String): String = {
        statements.map(_.generateCode(indent)).mkString("\n")
    }
}

sealed case class ExprStmt(expr: Expression) extends Statement {
    override def toString: String = s"$expr;"
}

sealed case class ForEachStmt(ty: String, variable: String, iterable: Expression, block: BlockStmt) extends Statement {
    override def generateCode(indent: String): String =
        s"""${indent}for (${CodeUtil.qualifiedClassName2Simple(ty)} $variable : $iterable) {
           |${block.generateCode(s"    $indent")}
           |$indent}""".stripMargin

    override def toString: String =
        s"""for (${CodeUtil.qualifiedClassName2Simple(ty)} $variable : $iterable) {
           |    $block
           |}""".stripMargin
}

sealed trait Expression extends Node

sealed trait NameOrHole extends Expression

sealed case class HoleExpr private(id: Int) extends NameOrHole

sealed case class EnumConstantExpr(enumType: BasicType, name: Expression) extends Expression {
    override def toString: String = s"${qualifiedClassName2Simple(enumType.ty)}.$name"
}

sealed case class MethodCallArgs(ty: Type, value: Expression) extends Expression {
    override def toString: String = value.toString
}

sealed case class MethodCallExpr private (receiver: Option[Expression], receiverType: BasicType, simpleName: String, args: Seq[MethodCallArgs]) extends Expression {
    override def toString: String = s"${receiver.map(r => s"$r.").getOrElse("")}$simpleName(${args.mkString(", ")})"

    def getQualifiedSignature = s"$receiverType.$simpleName(${args.map(_.ty).mkString(", ")})"
}

sealed case class ObjectCreationExpr private (receiverType: BasicType, args: Seq[MethodCallArgs]) extends Expression {
    override def toString: String = s"new $receiverType(${args.mkString(", ")})"
}

sealed case class NameExpr(name: String) extends NameOrHole {
    override def toString: String = name
}

sealed case class StaticFieldAccessExpr(receiverType: BasicType, targetType: Type, name: NameOrHole) extends Expression {
    override def toString: String = s"${qualifiedClassName2Simple(receiverType.ty)}.$name"
}

sealed case class VariableDeclaration(ty: Type, name: String, init: Option[Expression]) extends Expression {
    override def toString: String = init match {
        case None => s"${CodeUtil.qualifiedClassName2Simple(ty.toString)} $name"
        case Some(i) => s"${CodeUtil.qualifiedClassName2Simple(ty.toString)} $name = $i"
    }
}

sealed trait LiteralExpr extends Expression

sealed case class BooleanLiteral(value: Boolean) extends LiteralExpr {
    override def toString: String = value.toString
}

sealed case class ByteLiteral(value: Byte) extends LiteralExpr {
    override def toString: String = value.toString
}

sealed case class ShortLiteral(value: Short) extends LiteralExpr {
    override def toString: String = value.toString
}

sealed case class IntLiteral(value: Int) extends LiteralExpr {
    override def toString: String = value.toString
}

sealed case class LongLiteral(value: Long) extends LiteralExpr{
    override def toString: String = value.toString
}

sealed case class FloatLiteral(value: Float) extends LiteralExpr{
    override def toString: String = value.toString
}

sealed case class DoubleLiteral(value: Double) extends LiteralExpr{
    override def toString: String = value.toString
}

sealed case class CharLiteral(value: Char) extends LiteralExpr{
    override def toString: String = value.toString
}

sealed case class StringLiteral(value: String) extends LiteralExpr {
    override def toString: String = s"""\"$value\""""
}


object HoleExpr {
    private var nextId = 0

    def apply(): HoleExpr = {
        val hole = new HoleExpr(nextId)
        nextId += 1
        hole
    }
}