package com.github.woooking.cosyn.core.code

import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.core.recommend.Recommendation

trait HoleResolver {
    def resolve(context: Context, hole: HoleExpr, recommend: Boolean): Option[Question]
}


