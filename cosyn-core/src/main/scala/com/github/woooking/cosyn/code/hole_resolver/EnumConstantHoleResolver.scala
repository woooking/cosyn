package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, EnumConstantQA, HoleResolver, QA}
import com.github.woooking.cosyn.code.model.expr.{EnumConstantExpr, HoleExpr}
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

class EnumConstantHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: EnumConstantExpr =>
                Some(EnumConstantQA(p.enumType))
            case _ =>
                None
        }
    }
}
