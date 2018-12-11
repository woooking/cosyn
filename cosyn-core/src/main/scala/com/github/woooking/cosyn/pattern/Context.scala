package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.model.ASTCollector
import com.github.woooking.cosyn.pattern.model.expr.VariableDeclaration

import scala.collection.mutable

class Context(val extendedTypes: Seq[String]) {
    val variables: mutable.Buffer[(String, String)] = mutable.Buffer[(String, String)]()

    def findVariables(ty: String): Seq[String] = {
        variables
            .filter(p => KnowledgeGraph.isAssignable(p._2, ty))
            .map(_._1)
    }

    def update(pattern: Pattern): Unit = {
        val decls = new ASTCollector().collect[VariableDeclaration](pattern.stmts)
        decls.foreach(decl => variables += decl.name -> decl.ty)
    }
}
