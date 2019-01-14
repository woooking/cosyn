package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.code.model.BasicType
import com.github.woooking.cosyn.util.CodeUtil

class ReceiverHoleResolver extends HoleResolver {
    override def resolve(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = {
        pattern.parentOf(hole) match {
            case p: MethodCallExpr if p.receiver.contains(hole) =>
                val rawPaths = KnowledgeGraph.getIterablePaths(p.receiverType)
                val paths = rawPaths.filter(p => !rawPaths.exists(p2 => p.head.getExtendedTypes.contains(p2.head)))
                if (paths.size == 1) {
                    Some(QAHelper.choiceQAForType(context, p.receiverType))
                } else {
                    val requireObject = CodeUtil.qualifiedClassName2Simple(p.receiverType.ty).toLowerCase()
                    val question = paths
                        .map(p => if (p.size == 1) s"A $requireObject" else s"Some ${requireObject}s in a ${p.head.getSimpleName.toLowerCase()}")
                        .mkString("", "/", "?")
                    val vars = paths.map(_.head.getQualifiedName).map(BasicType.apply).flatMap(context.findVariables)
                    Some(ChoiceQuestion(question, vars.map(VariableChoice.apply).toSeq ++ paths.map(IterableChoice.apply).toSeq))
                }
            case _ =>
                None
        }
    }

    override def recommend(context: Context, pattern: Pattern, hole: HoleExpr): Option[Question] = ???
}