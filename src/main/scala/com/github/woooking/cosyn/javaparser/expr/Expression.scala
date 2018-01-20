package com.github.woooking.cosyn.javaparser.expr

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.expr.{
    Expression => JPExpression,
    AssignExpr => JPAssignExpr,
    BinaryExpr => JPBinaryExpr,
    BooleanLiteralExpr => JPBooleanLiteralExpr,
    CastExpr => JPCastExpr,
    CharLiteralExpr => JPCharLiteralExpr,
    FieldAccessExpr => JPFieldAccessExpr,
    IntegerLiteralExpr => JPIntegerLiteralExpr,
    MethodCallExpr => JPMethodCallExpr,
    NameExpr => JPNameExpr,
    NullLiteralExpr => JPNullLiteralExpr,
    ObjectCreationExpr => JPObjectCreationExpr,
    StringLiteralExpr => JPStringLiteralExpr,
    UnaryExpr => JPUnaryExpr,
    VariableDeclarationExpr => JPVariableDeclarationExpr,
}

trait Expression[T] extends NodeDelegate[T]

object Expression {
    def apply(expression: JPExpression): Expression[_] = expression match {
        case e: JPAssignExpr => AssignExpr(e)
        case e: JPBinaryExpr => BinaryExpr(e)
        case e: JPBooleanLiteralExpr => BooleanLiteralExpr(e)
        case e: JPCastExpr => CastExpr(e)
        case e: JPCharLiteralExpr => CharLiteralExpr(e)
        case e: JPFieldAccessExpr => FieldAccessExpr(e)
        case e: JPIntegerLiteralExpr => IntegerLiteralExpr(e)
        case e: JPMethodCallExpr => MethodCallExpr(e)
        case e: JPNameExpr => NameExpr(e)
        case e: JPNullLiteralExpr => NullLiteralExpr(e)
        case e: JPObjectCreationExpr => ObjectCreationExpr(e)
        case e: JPStringLiteralExpr => StringLiteralExpr(e)
        case e: JPUnaryExpr => UnaryExpr(e)
        case e: JPVariableDeclarationExpr => VariableDeclarationExpr(e)
    }
}