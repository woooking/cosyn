package com.github.woooking.cosyn.skeleton.model

import com.github.woooking.cosyn.util.{CodeUtil, StringUtil}
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

sealed case class ForEachStmt(ty: Type, variable: String, iterable: Expression, block: BlockStmt) extends Statement {
    override def generateCode(indent: String): String =
        s"""${indent}for (${CodeUtil.qualifiedClassName2Simple(ty.toString)} $variable : $iterable) {
           |${block.generateCode(s"    $indent")}
           |$indent}""".stripMargin

    override def toString: String =
        s"""for (${CodeUtil.qualifiedClassName2Simple(ty.toString)} $variable : $iterable) {
           |    $block
           |}""".stripMargin
}

sealed case class ReturnStmt(expr: Option[Expression]) extends Statement {
    override def toString: String = expr match {
        case Some(value) => s"return $value;"
        case None => "return;"
    }
}

sealed trait Expression extends Node

sealed trait NameOrHole extends Expression

sealed case class HoleExpr (id: Int) extends NameOrHole {
    override def toString: String = "<HOLE>"
}

sealed case class AssignExpr(name: NameExpr, target: Expression) extends Expression {
    override def toString: String = s"$name = $target"
}

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

sealed case class UnaryExpr(expr: Expression, ope: String, prefix: Boolean) extends Expression {
    override def toString: String = if (prefix) s"$ope$expr" else s"$expr$ope"
}

sealed trait NameExpr extends NameOrHole

sealed case class TyNameExpr(ty: Type, id: Int) extends NameExpr {
    override def toString: String = s"${StringUtil.decapitalize(CodeUtil.qualifiedClassName2Simple(ty.toString))}$id"
}

sealed case class SimpleNameExpr(name: String) extends NameExpr {
    override def toString: String = name
}

sealed case class StaticFieldAccessExpr(receiverType: BasicType, targetType: Type, name: NameOrHole) extends Expression {
    override def toString: String = s"${qualifiedClassName2Simple(receiverType.ty)}.$name"
}

sealed case class VariableDeclaration(ty: Type, name: NameExpr, init: Option[Expression]) extends Expression {
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

case class HoleFactory(var nextId: Int = 0) {
    def newHole(): HoleExpr = {
        val hole = HoleExpr(nextId)
        nextId += 1
        hole
    }
}
