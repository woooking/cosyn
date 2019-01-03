package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model.{BlockStmt, HoleExpr}

trait HoleResolver {
    def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[Question]
}


