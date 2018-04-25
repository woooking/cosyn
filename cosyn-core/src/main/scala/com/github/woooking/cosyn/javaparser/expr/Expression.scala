package com.github.woooking.cosyn.javaparser.expr

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.expr.{
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
    Expression => JPExpression,
    FieldAccessExpr => JPFieldAccessExpr,
    InstanceOfExpr => JPInstanceOfExpr,
    IntegerLiteralExpr => JPIntegerLiteralExpr,
    LambdaExpr => JPLambdaExpr,
    LongLiteralExpr => JPLongLiteralExpr,
    MarkerAnnotationExpr => JPMarkerAnnotationExpr,
    MethodCallExpr => JPMethodCallExpr,
    MethodReferenceExpr => JPMethodReferenceExpr,
    NameExpr => JPNameExpr,
    NullLiteralExpr => JPNullLiteralExpr,
    ObjectCreationExpr => JPObjectCreationExpr,
    StringLiteralExpr => JPStringLiteralExpr,
    SuperExpr => JPSuperExpr,
    ThisExpr => JPThisExpr,
    TypeExpr => JPTypeExpr,
    UnaryExpr => JPUnaryExpr,
    VariableDeclarationExpr => JPVariableDeclarationExpr
}

trait Expression[T <: Node] extends NodeDelegate[T]

object Expression {
    def apply(expression: JPExpression): Expression[_ <: Node] = expression match {
        case e: JPUnaryExpr if e.getOperator == JPUnaryExpr.Operator.MINUS && // case for -2147483638
            e.getExpression.isInstanceOf[JPIntegerLiteralExpr] &&
            e.getExpression.toString.equals("2147483648") =>
            IntegerLiteralExpr(new JPIntegerLiteralExpr(e.toString()))
        case e: JPUnaryExpr if e.getOperator == JPUnaryExpr.Operator.MINUS && // case for -9223372036854775808
            e.getExpression.isInstanceOf[JPLongLiteralExpr] &&
            (e.getExpression.toString.equals("9223372036854775808L") || e.getExpression.toString.equals("9223372036854775808l")) =>
            LongLiteralExpr(new JPLongLiteralExpr(e.toString()))
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
        case e: JPMarkerAnnotationExpr => MarkerAnnotationExpr(e)
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