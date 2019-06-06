package com.github.woooking.cosyn.core.nlp

import java.util.Properties

import com.github.woooking.cosyn.core.code.NlpContext
import com.github.woooking.cosyn.core.config.CoreConfig
import edu.stanford.nlp.pipeline.{CoreDocument, StanfordCoreNLPClient}

import scala.collection.JavaConverters._

object NLP {
    val props = new Properties()
    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, ner")
    props.setProperty("ner.buildEntityMentions", "false")
    val nlp = new StanfordCoreNLPClient(props, CoreConfig.global.nlpServerUri, CoreConfig.global.nlpServerPort)

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

    def jaccardSim(p1: Seq[String], p2: Seq[String]): Double = {
        val s1 = p1.toSet
        val s2 = p2.toSet
        (s1 & s2).size.toDouble / (s1 | s2).size.toDouble
    }

    def phraseWordSim(p: Seq[String], w: String): Double = {
        if (p.isEmpty) 0.0
        else p.map(word => wordSim(word, w)).max
    }

    def wordSim(word1: String, word2: String): Double = (word1.toLowerCase, word2.toLowerCase) match {
        case (w1, w2) if w1 == w2 => 1.0
        case (w1, w2) if w1.startsWith(w2) => 0.8
        case (w1, w2) if w2.startsWith(w1) => 0.8
        case (w1, w2) if removeVowel(w1).startsWith(w2) => 0.6
        case (w1, w2) if removeVowel(w2).startsWith(w1) => 0.6
        case _ => 0.0
    }

    def removeVowel(word: String): String = word.filterNot(w => "aeiou".contains(w))
}
