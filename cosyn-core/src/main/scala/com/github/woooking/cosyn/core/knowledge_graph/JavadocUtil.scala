package com.github.woooking.cosyn.core.knowledge_graph

object JavadocUtil {
    type JavadocDescriptionPreFilter = String => String

    val pTagReplace: JavadocDescriptionPreFilter = text => text.replaceAll("<p>", ".")
    val joinLines: JavadocDescriptionPreFilter = text => text.replaceAll("\n", " ")
    val joinSpaces: JavadocDescriptionPreFilter = text => text.replaceAll("[ ]+", " ")
    val javadocDescriptionPreFilters: Seq[JavadocDescriptionPreFilter] = Seq(pTagReplace, joinLines, joinSpaces)

    def extractFirstSentence(rawDescription: String): String = {
        val description = (rawDescription /: javadocDescriptionPreFilters) ((t, f) => f(t))
        NLP.getFirstSentence(description)
    }
}
