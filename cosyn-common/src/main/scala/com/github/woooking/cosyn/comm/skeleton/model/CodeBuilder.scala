package com.github.woooking.cosyn.comm.skeleton.model

trait FindNameContext {
    def nextIdForType(ty: Type): Int
}

trait CodeBuilder {
    def block(statements: Statement*): BlockStmt = {
        BlockStmt(statements.toSeq)
    }

    def assign(name: NameExpr, target: Expression): AssignExpr = AssignExpr(name, target)

    def when(condition: Expression, thenStmt: BlockStmt): IfStmt = IfStmt(condition, thenStmt, None)

    def when(condition: Expression, thenStmt: BlockStmt, elseStmt: BlockStmt): IfStmt = IfStmt(condition, thenStmt, Some(elseStmt))

    def forStmt(inits: Seq[Expression], updates: List[Expression], block: BlockStmt): ForStmt = ForStmt(inits, None, updates, block)

    def forStmt(inits: Seq[Expression], condition: Expression, updates: List[Expression], block: BlockStmt): ForStmt = ForStmt(inits, Some(condition), updates, block)

    def foreach(ty: Type, variable: String, iterable: Expression, block: BlockStmt): ForEachStmt = ForEachStmt(ty, variable, iterable, block)

    def enum(enumType: BasicType, name: NameOrHole): EnumConstantExpr = EnumConstantExpr(enumType, name)

    def arg(ty: Type, value: Expression): MethodCallArgs = MethodCallArgs(ty, value)

    def call(receiver: Expression, receiverType: BasicType, simpleName: String, args: MethodCallArgs*): MethodCallExpr = MethodCallExpr(Some(receiver), receiverType, simpleName, args)

    def call(receiverType: BasicType, simpleName: String, args: MethodCallArgs*): MethodCallExpr = MethodCallExpr(None, receiverType, simpleName, args)

    def create(receiverType: BasicType, args: MethodCallArgs*): ObjectCreationExpr = ObjectCreationExpr(receiverType, args)

    def create(basicType: Type, initializers: List[Expression]): ArrayCreationExpr = ArrayCreationExpr(basicType, initializers)

    def field(receiverType: BasicType, targetType: Type, name: NameOrHole): StaticFieldAccessExpr = StaticFieldAccessExpr(receiverType, targetType, name)

    def field(receiverType: BasicType, receiver: Expression, name: NameOrHole): FieldAccessExpr = FieldAccessExpr(receiverType, receiver, name)

    def name(ty: Type)(implicit ctx: FindNameContext): NameExpr = TyNameExpr(ty, ctx.nextIdForType(ty))

    def whileStmt(condition: Expression, block: BlockStmt): WhileStmt = WhileStmt(condition, block)

    def ret(): ReturnStmt = ReturnStmt(None)

    def ret(expr: Expression): ReturnStmt = ReturnStmt(Some(expr))

    def unary(expr: Expression, ope: String, prefix: Boolean) = UnaryExpr(expr, ope, prefix)

    def binary(ope: String, left: Expression, right: Expression) = BinaryExpr(ope, left, right)

    def v(ty: Type, name: NameExpr): VariableDeclaration = VariableDeclaration(ty, name, None)

    def v(ty: Type, name: NameExpr, init: Expression): VariableDeclaration = VariableDeclaration(ty, name, Some(init))
}

object CodeBuilder extends CodeBuilder {
    implicit def expr2stmt(expr: Expression): ExprStmt = ExprStmt(expr)

    implicit def string2type(s: String): BasicType = BasicType(s)

    implicit def string2name(s: String): SimpleNameExpr = SimpleNameExpr(s)
}
