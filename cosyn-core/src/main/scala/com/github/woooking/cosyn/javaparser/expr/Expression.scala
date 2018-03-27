package com.github.woooking.cosyn.javaparser.expr

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.expr.{
    Expression => JPExpression,
    ArrayAccessExpr => JPArrayAccessExpr,
    ArrayCreationExpr => JPArrayCreationExpr,
    ArrayInitializerExpr => JPArrayInitializerExpr,
    AssignExpr => JPAssignExpr,
    BinaryExpr => JPBinaryExpr,
    BooleanLiteralExpr => JPBooleanLiteralExpr,
    CastExpr => JPCastExpr,
    CharLiteralExpr => JPCharLiteralExpr,
    ClassExpr => JPClassExpr,
    ConditionalExpr => JPConditionalExpr,
    DoubleLiteralExpr => JPDoubleLiteralExpr,
    EnclosedExpr => JPEnclosedExpr,
    FieldAccessExpr => JPFieldAccessExpr,
    InstanceOfExpr => JPInstanceOfExpr,
    IntegerLiteralExpr => JPIntegerLiteralExpr,
    LambdaExpr => JPLambdaExpr,
    LongLiteralExpr => JPLongLiteralExpr,
    MethodCallExpr => JPMethodCallExpr,
    MethodReferenceExpr => JPMethodReferenceExpr,
    NameExpr => JPNameExpr,
    NullLiteralExpr => JPNullLiteralExpr,
    ObjectCreationExpr => JPObjectCreationExpr,
    StringLiteralExpr => JPStringLiteralExpr,
    SuperExpr => JPSuperExpr,
    ThisExpr => JPThisExpr,
    UnaryExpr => JPUnaryExpr,
    VariableDeclarationExpr => JPVariableDeclarationExpr,
    TypeExpr => JPTypeExpr,
}

trait Expression[T] extends NodeDelegate[T]

object Expression {
    def apply(expression: JPExpression): Expression[_] = expression match {
        case e: JPArrayAccessExpr => ArrayAccessExpr(e)
        case e: JPArrayCreationExpr => ArrayCreationExpr(e)
        case e: JPArrayInitializerExpr => ArrayInitializerExpr(e)
        case e: JPAssignExpr => AssignExpr(e)
        case e: JPBinaryExpr => BinaryExpr(e)
        case e: JPBooleanLiteralExpr => BooleanLiteralExpr(e)
        case e: JPCastExpr => CastExpr(e)
        case e: JPCharLiteralExpr => CharLiteralExpr(e)
        case e: JPClassExpr => ClassExpr(e)
        case e: JPConditionalExpr => ConditionalExpr(e)
        case e: JPDoubleLiteralExpr => DoubleLiteralExpr(e)
        case e: JPEnclosedExpr => EnclosedExpr(e)
        case e: JPFieldAccessExpr => FieldAccessExpr(e)
        case e: JPInstanceOfExpr => InstanceOfExpr(e)
        case e: JPIntegerLiteralExpr => IntegerLiteralExpr(e)
        case e: JPLambdaExpr => LambdaExpr(e)
        case e: JPLongLiteralExpr => LongLiteralExpr(e)
        case e: JPMethodCallExpr => MethodCallExpr(e)
        case e: JPMethodReferenceExpr => MethodReferenceExpr(e)
        case e: JPNameExpr => NameExpr(e)
        case e: JPNullLiteralExpr => NullLiteralExpr(e)
        case e: JPObjectCreationExpr => ObjectCreationExpr(e)
        case e: JPThisExpr => ThisExpr(e)
        case e: JPStringLiteralExpr => StringLiteralExpr(e)
        case e: JPSuperExpr => SuperExpr(e)
        case e: JPUnaryExpr => UnaryExpr(e)
        case e: JPVariableDeclarationExpr => VariableDeclarationExpr(e)
        case e: JPTypeExpr => TypeExpr(e)
    }
}