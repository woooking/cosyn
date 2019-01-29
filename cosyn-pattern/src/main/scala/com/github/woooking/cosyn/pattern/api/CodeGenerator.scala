package com.github.woooking.cosyn.pattern.api

import de.parsemis.graph.{Graph => ParsemisGraph}

trait CodeGenerator[N, E, Graph, R] {
    def generate(originalGraph: Seq[Graph])(graph: ParsemisGraph[N, E]): R
}
