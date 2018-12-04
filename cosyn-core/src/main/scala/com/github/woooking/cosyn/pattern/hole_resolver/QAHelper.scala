package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern._
import com.github.woooking.cosyn.pattern.model.expr.HoleExpr
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil

object QAHelper {
    private sealed trait MethodType

    private case class ConstructorType(ty: String) extends MethodType

    private case class GetType(ty: String) extends MethodType

    private case class StaticType(ty: String) extends MethodType

    private case object OtherType extends MethodType

    def choiceQAForType(context: Context, referenceType: String): QA = {
        val vars = context.findVariables(referenceType)
        val producers = KnowledgeGraph.producers(context, referenceType)
        val cases = producers.groupBy {
            case m if m.isConstructor => ConstructorType(m.getDeclareType.getQualifiedName)
            case m if m.isStatic => StaticType(m.getDeclareType.getQualifiedName)
            case m if CodeUtil.isGetMethod(m.getSimpleName) => GetType(m.getDeclareType.getQualifiedName)
            case _ => OtherType
        }
        val methodChoices = cases.flatMap {
            case (ConstructorType(ty), ms) => Seq(ConstructorChoice(ty, ms))
            case (StaticType(ty), ms) => Seq(StaticChoice(ty, ms))
            case (GetType(ty), ms) => ms.map(m => GetChoice(ty, m))
            case (OtherType, m) =>
                m.map(_.getQualifiedSignature).foreach(println)
                Seq()
        }
        val simpleName = CodeUtil.qualifiedClassName2Simple(referenceType).toLowerCase
        val q = s"Which $simpleName?"
        ChoiceQA(q, vars.map(VariableChoice.apply) ++ methodChoices)
    }
}
