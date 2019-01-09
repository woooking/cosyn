package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.code.model._
import com.github.woooking.cosyn.code.model.ty.Type
import com.github.woooking.cosyn.code.model.visitors.VariableCollector
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph

case class Context(variables: Seq[(String, Type)], extendedTypes: Seq[String]) {
    def findVariables(ty: Type): Seq[String] = {
        variables
            .filter(p => KnowledgeGraph.isAssignable(p._2, ty))
            .map(_._1)
    }

    def update(pattern: Pattern): Context = {
        this.copy(variables = variables ++ VariableCollector.instance[BlockStmt].collect(pattern.stmts))
    }
}
