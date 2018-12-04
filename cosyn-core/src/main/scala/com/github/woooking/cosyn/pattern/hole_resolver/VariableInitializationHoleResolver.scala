package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.pattern._
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, VariableDeclaration}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt

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
