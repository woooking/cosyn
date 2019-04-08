package com.github.woooking.cosyn.core.code

import edu.stanford.nlp.ling.CoreLabel
import edu.stanford.nlp.semgraph.SemanticGraph

case class NlpContext(semanticGraph: SemanticGraph, coreLabels: List[CoreLabel]) {

}
