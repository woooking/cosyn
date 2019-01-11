package com.github.woooking.cosyn.knowledge_graph

import com.github.woooking.cosyn.code.{Choice, Context}
import com.github.woooking.cosyn.code.model.{ArrayType, BasicType, Type}

object Recommendation {
    def recommend(context: Context, ty: Type): List[Choice] = ty match {
        case BasicType(ty) =>
            Nil
        case ArrayType(componentType) =>
            Nil
    }
}
