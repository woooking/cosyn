package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern._
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil

class ReceiverHoleResolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: MethodCallExpr if p.receiver.contains(hole) =>
                val rawPaths = KnowledgeGraph.getIterablePaths(p.receiverType)
                val paths = rawPaths.filter(p => !rawPaths.exists(p2 => p.head.getExtendedTypes.contains(p2.head)))
                if (paths.size == 1) {
                    Some(QAHelper.choiceQAForType(context, p.receiverType))
                } else {
                    val question = paths
                        .map(p => CodeUtil.qualifiedClassName2Simple(p.head.getQualifiedName).toLowerCase())
                        .map(b => s"A $b")
                        .mkString("", "/", "?")
                    Some(ChoiceQA(question, paths.map(IterableChoice.apply).toSeq))
                }
            case _ =>
                None
        }
    }
}
