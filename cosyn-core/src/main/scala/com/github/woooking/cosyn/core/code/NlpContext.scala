package com.github.woooking.cosyn.core.code

import com.github.woooking.cosyn.comm.skeleton.model._
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.core.Components
import com.github.woooking.cosyn.core.nlp.NLP
import edu.stanford.nlp.ling.{CoreAnnotations, CoreLabel}
import edu.stanford.nlp.semgraph.SemanticGraph

import scala.collection.JavaConverters._

case class NlpContext(semanticGraph: SemanticGraph, coreLabels: List[CoreLabel]) {
    private val methodEntityRepository = Components.methodEntityRepository
    private val typeEntityRepository = Components.typeEntityRepository

    def enumSim(constant: String): Double = {
        val textSim = coreLabels.map(_.originalText()).map(NLP.wordSim(_, constant)).max
        val nerSim = coreLabels.map(_.ner()).map(NLP.wordSim(_, constant)).max
        math.max(textSim, nerSim)
    }

    def recommendEnum(ty: BasicType): List[(String, Double)] = {
        typeEntityRepository.enumConstants(ty)
            .map(c => c -> enumSim(c))
            .filter(_._2 > 0.5)
            .toList
    }

    def recommendPrimitive(method: MethodCallExpr, arg: MethodCallArgs, ty: String): List[(String, Double)] = {
        val methodEntity = methodEntityRepository.getMethod(method.getQualifiedSignature)
        val index = method.findArg(arg)
        val paramName = methodEntity.getParamNames.split(",")(index)
        val paramPhrase = CodeUtil.lowerCamelCaseToPhrase(paramName)
        val paramJavadoc = methodEntity.getParamJavadoc(paramName)
        val javadocPhrase = if (paramJavadoc == null) Array[String]() else paramJavadoc.split(" ")

        ty match {
            case "boolean" =>
                Nil
            case "byte" | "short" | "int" | "long" =>
                type NumAnno = (CoreLabel, Number)
                coreLabels.map[NumAnno, List[NumAnno]](label => label -> label.get(classOf[CoreAnnotations.NumericCompositeValueAnnotation]))
                    .filter(_._2 != null)
                    .map(l => {
                        val word = semanticGraph.getNodeByIndex(l._1.index())
                        val score = semanticGraph.getIncomingEdgesSorted(word).asScala
                            .map(_.getSource)
                            .map(_.originalText())
                            .map(word => math.max(NLP.phraseWordSim(paramPhrase, word), NLP.phraseWordSim(javadocPhrase, word)))
                            .max
                        (l._2.longValue() - 1).toString -> score
                    })
                    .filter(_._2 > 0.5)
            case "float" | "double" =>
                Nil
            case "char" =>
                Nil
            case "java.lang.String" =>
                Nil
        }
    }
}
