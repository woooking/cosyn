package com.github.woooking.cosyn.pattern.rules

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.knowledge_graph.JavadocUtil

object GetMethodJudger extends PositiveJudger[MethodEntity] {
    // 方法的简单名以get开头，之后是一个大写字母或结尾
    private val nameRule: Rule = methodEntity => methodEntity.getSimpleName.matches("^(get)([A-Z].*|$)")

    override def rules: Seq[Rule] = Seq(nameRule)
}
