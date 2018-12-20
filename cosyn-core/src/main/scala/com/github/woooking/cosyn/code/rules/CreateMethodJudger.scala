package com.github.woooking.cosyn.code.rules

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.knowledge_graph.JavadocUtil

object CreateMethodJudger extends PositiveJudger[MethodEntity] {
    // 方法的简单名以create或new开头，之后是一个大写字母或结尾
    private val nameRule: Rule = methodEntity => methodEntity.getSimpleName.matches("^(create|new)([A-Z].*|$)")

    // javadoc的第一句话中包含create
    private val javadocRule: Rule = methodEntity => JavadocUtil.extractFirstSentence(methodEntity.getJavadoc).toLowerCase.contains("create")

    override def rules: Seq[Rule] = Seq(nameRule, javadocRule)
}
