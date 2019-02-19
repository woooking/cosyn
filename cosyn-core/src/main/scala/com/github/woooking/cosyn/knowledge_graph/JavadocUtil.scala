package com.github.woooking.cosyn.knowledge_graph

import com.github.javaparser.JavaParser

import scala.collection.JavaConverters._
import scala.compat.java8.OptionConverters._

object JavadocUtil {
    type JavadocDescriptionPreFilter = String => String

    val pTagReplace: JavadocDescriptionPreFilter = text => text.replaceAll("<p>", ".")
    val joinLines: JavadocDescriptionPreFilter = text => text.replaceAll("\n", " ")
    val joinSpaces: JavadocDescriptionPreFilter = text => text.replaceAll("[ ]+", " ")
    val javadocDescriptionPreFilters: Seq[JavadocDescriptionPreFilter] = Seq(pTagReplace, joinLines, joinSpaces)

    def extractParamInfoFromJavadoc(index: Int)(javadocComment: String): String = {
        val javadoc = JavaParser.parseJavadoc(javadocComment)
        val paramTags = javadoc.getBlockTags.asScala.filter(_.getTagName == "param")
        if (paramTags.length <= index) return ""
        val tag = paramTags(index)
        NLP.getNounPhrase(tag.getName.get(), tag.getContent.toText)
    }

    def extractReturnInfoFromJavadoc(javadocComment: String): String = {
        val javadoc = JavaParser.parseJavadoc(javadocComment)
        val paramTags = javadoc.getBlockTags.asScala.filter(_.getTagName == "return")
        val tag = paramTags.headOption.get
        tag.getContent.toText
    }

    def extractFirstSentence(javadocComment: String): String = {
        val javadoc = JavaParser.parseJavadoc(javadocComment)
        val description = (javadoc.getDescription.toText /: javadocDescriptionPreFilters) ((t, f) => f(t))
        NLP.getFirstSentence(description)
    }
}
