package com.github.woooking.cosyn.core.code.rules

import com.github.woooking.cosyn.comm.skeleton.model.BasicType
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.core.Components
import com.github.woooking.cosyn.core.nlp.JavadocUtil
import com.github.woooking.cosyn.kg.entity.MethodEntity

object LoadMethodJudger extends PositiveJudger[MethodEntity] {
    private val typeEntityRepository = Components.typeEntityRepository
    // 方法的简单名以load, read, open开头，之后是一个大写字母或结尾
    private val nameRule: Rule = methodEntity => methodEntity.getSimpleName.matches("^(load|read|open)([A-Z].*|$)")

    // javadoc的第一句话中包含load, read, open
    private val javadocRule: Rule = methodEntity => {
        val javadoc = Option(methodEntity.getJavadoc).map(_.getDescription).map(JavadocUtil.extractFirstSentence).getOrElse("").toLowerCase
        javadoc.contains("create") || javadoc.contains("read") || javadoc.contains("open")
    }

    // 参数中有java.io.InputStream的子类
    private val paramRule: Rule = methodEntity =>
        CodeUtil.methodParams(methodEntity.getQualifiedSignature)
            .exists(param => typeEntityRepository.isAssignable(param, BasicType("java.io.InputStream")))

    override def rules: Seq[Rule] = Seq(nameRule, javadocRule, paramRule)
}
