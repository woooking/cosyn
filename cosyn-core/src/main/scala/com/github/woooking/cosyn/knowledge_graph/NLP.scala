package com.github.woooking.cosyn.knowledge_graph

import java.util.Properties

import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLP}
import edu.stanford.nlp.semgraph.SemanticGraph

import scala.collection.JavaConverters._

object NLP {
//    val props = new Properties()
//    props.setProperty("annotators", "tokenize, ssplit, pos, parse, depparse")
//    val nlp = new StanfordCoreNLP(props)

    type SpecialPreFilter = (String, String) => String

    val ofRule: SpecialPreFilter = (param, text) =>
        if (text.startsWith("of")) s"$param $text"
        else text

    val preFilters = Seq(ofRule)

    def getNounPhrase(param: String, text: String): String = {
        val filtered = (text /: preFilters) ((s, f) => f(param, s))
//        val document = new CoreDocument(filtered)
//        nlp.annotate(document)
//        val sentence = document.sentences().get(0)
//        val dependencyParse: SemanticGraph = sentence.dependencyParse
//        System.out.println("Example: dependency parse")
//        System.out.println(dependencyParse)
        filtered
    }
}
