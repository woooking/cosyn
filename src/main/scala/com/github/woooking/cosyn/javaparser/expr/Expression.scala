package com.github.woooking.cosyn.javaparser.expr

import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.javaparser.ast.expr.{
    Expression => JPExpression,
    AssignExpr => JPAssignExpr,
    MethodCallExpr => JPMethodCallExpr,
    VariableDeclarationExpr => JPVariableDeclarationExpr,
}

trait Expression extends NodeDelegate[JPExpression]

object Expression {
    def apply(expression: JPExpression): Expression = expression match {
        case e: JPAssignExpr => AssignExpr(e)
        case e: JPVariableDeclarationExpr => VariableDeclarationExpr(e)
        case e: JPMethodCallExpr => MethodCallExpr(e)
    }
}