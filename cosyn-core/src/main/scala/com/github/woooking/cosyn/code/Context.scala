package com.github.woooking.cosyn.code

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model._
import com.github.woooking.cosyn.skeleton.model.Type
import com.github.woooking.cosyn.skeleton.model.visitors.VariableCollector
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.util.CodeUtil

case class Context(variables: Set[(String, Type)], extendedTypes: Seq[String]) {
    def findVariables(ty: Type): Set[String] = {
        variables
            .filter(p => KnowledgeGraph.isAssignable(p._2, ty))
            .map(_._1)
    }

    def update(pattern: Pattern): Context = {
        this.copy(variables = variables ++ VariableCollector.instance[BlockStmt].collect(pattern.stmts))
    }

    def update(name: String, ty: Type): Context = {
        this.copy(variables = variables + (name -> ty))
    }

    def findFreeVariableName(ty: Type): String = {
        ty match {
            case BasicType(bt) =>
                val simpleName = CodeUtil.qualifiedClassName2Simple(bt)
                val prefix = simpleName.updated(0, simpleName.charAt(0).toLower)
                ("" #:: Stream.from(2).map(_.toString))
                    .map(s => s"$prefix$s")
                    .find(s => !variables.exists(_._1 == s)).get
            case ArrayType(componentType) =>
                val bt = CodeUtil.coreType(componentType)
                val simpleName = CodeUtil.qualifiedClassName2Simple(bt)
                val prefix = simpleName.updated(0, simpleName.charAt(0).toLower) + "s"
                ("" #:: Stream.from(2).map(_.toString))
                    .map(s => s"$prefix$s")
                    .find(s => !variables.exists(_._1 == s)).get
        }
    }
}
