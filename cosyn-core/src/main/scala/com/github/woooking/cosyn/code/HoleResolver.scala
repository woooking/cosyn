package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model.HoleExpr

trait HoleResolver {
    def resolve(pattern: Pattern, hole: HoleExpr, context: Context): Option[Question]
}


