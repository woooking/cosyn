package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, EnumConstantQuestion, HoleResolver, Question}
import com.github.woooking.cosyn.code.model.expr.HoleExpr
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

class EnumConstantHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[Question] = {
        hole.parent match {
            case p: EnumConstantExpr =>
                Some(EnumConstantQuestion(p.enumType))
            case _ =>
                None
        }
    }
}
