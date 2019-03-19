package com.github.woooking.cosyn.comm.config

import com.github.woooking.cosyn.comm.skeleton.model._
import org.json4s.native.Serialization
import org.json4s.{Formats, ShortTypeHints}

object JsonConfig {
    implicit val formats: Formats = Serialization.formats(ShortTypeHints(
        classOf[BlockStmt] ::
            classOf[ExprStmt] ::
            classOf[ForEachStmt] ::
            classOf[ReturnStmt] ::
            classOf[HoleExpr] ::
            classOf[AssignExpr] ::
            classOf[EnumConstantExpr] ::
            classOf[MethodCallArgs] ::
            classOf[MethodCallExpr] ::
            classOf[ObjectCreationExpr] ::
            classOf[UnaryExpr] ::
            classOf[TyNameExpr] ::
            classOf[SimpleNameExpr] ::
            classOf[StaticFieldAccessExpr] ::
            classOf[VariableDeclaration] ::
            classOf[BooleanLiteral] ::
            classOf[ByteLiteral] ::
            classOf[ShortLiteral] ::
            classOf[IntLiteral] ::
            classOf[LongLiteral] ::
            classOf[FloatLiteral] ::
            classOf[DoubleLiteral] ::
            classOf[CharLiteral] ::
            classOf[StringLiteral] ::
            classOf[BasicType] ::
            classOf[ArrayType] ::
            Nil
    ))
}

