package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, HoleResolver, QA}
import com.github.woooking.cosyn.code.model.expr.{HoleExpr, VariableDeclaration}
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

class VariableInitializationHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: VariableDeclaration =>
                Some(QAHelper.choiceQAForType(context, p.ty))
            case _ =>
                None
        }
    }
}
