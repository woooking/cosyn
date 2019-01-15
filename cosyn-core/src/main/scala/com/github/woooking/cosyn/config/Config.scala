package com.github.woooking.cosyn.config

import com.github.woooking.cosyn.code.hole_resolver._

object Config {
    // ----- 调试参数 -----
    val printCodeEachStep: Boolean = true
    // ----- 算法参数 -----
    val maxSearchStep: Int = 2
    val holeResolver = CombineHoleResolver(
        new EnumConstantHoleResolver,
        new StaticFieldAccessHoleResolver,
        new ReceiverHoleResolver,
        new ArgumentHoleResolver,
        new VariableInitializationHoleResolver,
    )
}
