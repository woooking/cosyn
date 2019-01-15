package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.knowledge_graph.{KnowledgeGraph, Recommendation}
import com.github.woooking.cosyn.code.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.code.rules.{CreateMethodJudger, GetMethodJudger, LoadMethodJudger}
import com.github.woooking.cosyn.util.CodeUtil

object QAHelper {
    private sealed trait MethodType

    sealed abstract class MethodCategory(val questionGenerator: String => String)

    object MethodCategory {
        final case object Create extends MethodCategory(ty => s"Create a new ${CodeUtil.qualifiedClassName2Simple(ty)}")
        final case object Load extends MethodCategory(ty => s"Load a ${CodeUtil.qualifiedClassName2Simple(ty)}")
        final case object Get extends MethodCategory(ty => s"Get a ${CodeUtil.qualifiedClassName2Simple(ty)}")
    }

    private final case object OtherType extends MethodCategory(_ => "Unknown")

    def choiceQAForType(context: Context, ty: Type): ChoiceQuestion = {
        ty match {
            case bt @ BasicType(t) =>
                val vars = context.findVariables(bt)
                val producers = KnowledgeGraph.producers(context, bt)

                val cases: Map[MethodCategory, Set[MethodEntity]] = producers.groupBy {
                    case m if CreateMethodJudger.judge(m) => MethodCategory.Create
                    case m if LoadMethodJudger.judge(m) => MethodCategory.Load
                    case m if GetMethodJudger.judge(m) => MethodCategory.Get
                    case _ =>
                        OtherType
                }
                val methodCategoryChoices = cases.flatMap {
                    case (OtherType, m) =>
                        println("-----")
                        m.foreach(f => {
                            println(f.getQualifiedSignature)
                            println(KnowledgeGraph.getMethodJavadoc(f.getQualifiedSignature).getOrElse(""))
                        })
                        println("-----")
                        Seq()
                    case (category, ms) => Seq(MethodCategoryChoice(bt, category, ms))
                }
                val simpleName = CodeUtil.qualifiedClassName2Simple(t).toLowerCase
                val q = s"Which $simpleName?"
                ChoiceQuestion(q, vars.map(VariableChoice.apply) ++ methodCategoryChoices)
            case at @ ArrayType(BasicType(t)) =>
                ???
            case _ =>
                ???
        }

    }
}
