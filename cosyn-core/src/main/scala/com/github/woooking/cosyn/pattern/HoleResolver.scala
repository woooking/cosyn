package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.pattern.model.expr.{Expression, HoleExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt

trait HoleResolver {
    def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA]
}


