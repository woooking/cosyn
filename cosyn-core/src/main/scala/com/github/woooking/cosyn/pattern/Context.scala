package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph

import collection.mutable

class Context {
    val variables: mutable.Buffer[(String, String)] = mutable.Buffer[(String, String)]()

    def findVariables(ty: String): Seq[String] = {
        variables
            .filter(p => KnowledgeGraph.isAssignable(p._2, ty))
            .map(_._1)
    }
}
