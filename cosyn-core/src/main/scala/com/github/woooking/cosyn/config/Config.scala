package com.github.woooking.cosyn.config

import com.github.woooking.cosyn.code.hole_resolver._

object Config {
    // ----- 调试参数 -----
    val printCodeEachStep: Boolean = true
    val printUnCategorisedMethods: Boolean = false
    // ----- 算法参数 -----
    val maxSearchStep: Int = 3
    val recommendNumber: Int = 5
    val iterablePenalty: Double = 0.5
    val holeResolver = CombineHoleResolver(
        new EnumConstantHoleResolver,
        new StaticFieldAccessHoleResolver,
        new ReceiverHoleResolver,
        new ArgumentHoleResolver,
        new VariableInitializationHoleResolver,
    )
}
