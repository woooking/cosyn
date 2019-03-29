package com.github.woooking.cosyn.core.knowledge_graph

import java.util.Properties

import com.github.woooking.cosyn.core.code.NlpContext
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLPClient}

import scala.collection.JavaConverters._

object NLP {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, ner")
    props.setProperty("ner.buildEntityMentions", "false")
    val nlp = new StanfordCoreNLPClient(props, "http://162.105.88.236", 9000)

    type SpecialPreFilter = (String, String) => String

    val ofRule: SpecialPreFilter = (param, text) =>
        if (text.startsWith("of")) s"$param $text"
        else text

    val preFilters: Seq[SpecialPreFilter] = Seq(ofRule)

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

    def parse(text: String): NlpContext = {
        val annotation = nlp.process(text)
        val document = new CoreDocument(annotation)
        val sentence = document.sentences().get(0)
        NlpContext(sentence.dependencyParse(), sentence.tokens().asScala.toList)
    }

    def getFirstSentence(text: String): String = {
        val annotation = nlp.process(text)
        val document = new CoreDocument(annotation)
        val sentences = document.sentences()
        if (sentences != null && sentences.size > 0) sentences.get(0).text() else ""
    }
}
