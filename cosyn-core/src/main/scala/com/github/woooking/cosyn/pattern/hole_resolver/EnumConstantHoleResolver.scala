package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.pattern.model.expr.{EnumConstantExpr, HoleExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.{Context, EnumConstantQA, HoleResolver, QA}

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
