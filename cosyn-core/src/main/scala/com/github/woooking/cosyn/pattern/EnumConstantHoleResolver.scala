package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.model.expr.{EnumConstantExpr, HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil

class EnumConstantHoleResolver extends HoleResolver {
    private sealed trait MethodType

    private case class ConstructorType(ty: String) extends MethodType

    private case class GetType(ty: String) extends MethodType

    private case class StaticType(ty: String) extends MethodType

    private case object OtherType extends MethodType

    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: EnumConstantExpr =>
                Some(EnumConstantQA(p.enumType))
            case _ =>
                None
        }
    }
}
