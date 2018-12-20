package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.ASTCollector
import com.github.woooking.cosyn.code.model.expr.VariableDeclaration
import com.github.woooking.cosyn.code.model.ty.Type

import scala.collection.mutable

class Context(val extendedTypes: Seq[String]) {
    val variables: mutable.Buffer[(String, Type)] = mutable.Buffer()

    def findVariables(ty: Type): Seq[String] = {
        variables
            .filter(p => KnowledgeGraph.isAssignable(p._2, ty))
            .map(_._1)
    }

    def update(pattern: Pattern): Unit = {
        val decls = new ASTCollector().collect[VariableDeclaration](pattern.stmts)
        decls.foreach(decl => variables += decl.name -> decl.ty)
    }
}
