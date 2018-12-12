package com.github.woooking.cosyn.pattern.rules

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph}
import com.github.woooking.cosyn.pattern.model.ty.BasicType
import com.github.woooking.cosyn.util.CodeUtil

object LoadMethodJudger extends PositiveJudger[MethodEntity] {
    // 方法的简单名以load, read, open开头，之后是一个大写字母或结尾
    private val nameRule: Rule = methodEntity => methodEntity.getSimpleName.matches("^(load|read|open)([A-Z].*|$)")

    // javadoc的第一句话中包含load, read, open
    private val javadocRule: Rule = methodEntity => {
        val javadoc = JavadocUtil.extractFirstSentence(methodEntity.getJavadoc).toLowerCase
        javadoc.contains("create") || javadoc.contains("read") || javadoc.contains("open")
    }

    // 参数中有java.io.InputStream的子类
    private val paramRule: Rule = methodEntity =>
        CodeUtil.methodParams(methodEntity.getQualifiedSignature)
            .exists(param => KnowledgeGraph.isAssignable(param, BasicType("java.io.InputStream")))

    override def rules: Seq[Rule] = Seq(nameRule, javadocRule, paramRule)
}
