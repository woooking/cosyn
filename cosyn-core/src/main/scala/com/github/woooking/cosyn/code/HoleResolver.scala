package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model.HoleExpr

trait HoleResolver {
    def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question]

    def recommend(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question]
}


