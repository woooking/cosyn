package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.ty.{BasicType, Type}

trait CodeBuilder {
    def block(statements: Statement*): BlockStmt = {
        BlockStmt(statements.toSeq)
    }

    def foreach(ty: String, variable: String, iterable: Expression, block: BlockStmt): ForEachStmt = ForEachStmt(ty, variable, iterable, block)

    def enum(enumType: BasicType, name: NameOrHole): EnumConstantExpr = EnumConstantExpr(enumType, name)

    def arg(ty: Type, value: Expression): MethodCallArgs = MethodCallArgs(ty, value)

    def call(receiver: Expression, receiverType: BasicType, simpleName: String, args: MethodCallArgs*): MethodCallExpr = MethodCallExpr(Some(receiver), receiverType, simpleName, args)

    def field(receiverType: BasicType, targetType: Type, name: NameOrHole): StaticFieldAccessExpr = StaticFieldAccessExpr(receiverType, targetType, name)

    def v(ty: Type, name: String): VariableDeclaration = VariableDeclaration(ty, name, None)

    def v(ty: Type, name: String, init: Expression): VariableDeclaration = VariableDeclaration(ty, name, Some(init))
}

object CodeBuilder extends CodeBuilder {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)

    implicit def string2type(s: String): BasicType = BasicType(s)

    implicit def str2expr(name: String): NameExpr = NameExpr(name)
}
