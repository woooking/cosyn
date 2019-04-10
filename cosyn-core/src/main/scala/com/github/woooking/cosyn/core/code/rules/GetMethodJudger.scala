package com.github.woooking.cosyn.core.code.rules

import com.github.woooking.cosyn.core.nlp.JavadocUtil
import com.github.woooking.cosyn.kg.entity.MethodEntity

object GetMethodJudger extends PositiveJudger[MethodEntity] {
    // 方法的简单名以get开头，之后是一个大写字母或结尾
    private val nameRule: Rule = methodEntity => methodEntity.getSimpleName.matches("^(get)([A-Z].*|$)")

    override def rules: Seq[Rule] = Seq(nameRule)
}
