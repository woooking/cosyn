package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model.expr.{Expression, HoleExpr}
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

trait HoleResolver {
    def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA]
}


