package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code.{Context, HoleResolver, PrimitiveQA, QA}
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph}
import com.github.woooking.cosyn.code.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.code.model.stmt.BlockStmt
import com.github.woooking.cosyn.code.model.ty.Type.PrimitiveOrString


class ArgumentHoleResolver extends HoleResolver {
    private sealed trait MethodType

    private case class ConstructorType(ty: String) extends MethodType

    private case class GetType(ty: String) extends MethodType

    private case class StaticType(ty: String) extends MethodType

    private case object OtherType extends MethodType

    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: MethodCallExpr =>
                p.args.indexWhere(_.value == hole) match {
                    case index if index != -1 =>
                        val arg = p.args(index)
                        val vars = context.findVariables(arg.ty)
                        arg.ty match {
                            case PrimitiveOrString(ty) =>
                                Some(PrimitiveQA(
                                    KnowledgeGraph.getMethodJavadoc(p.getQualifiedSignature).map(JavadocUtil.extractParamInfoFromJavadoc(index)),
                                    ty
                                ))
                            case _ =>
                                Some(QAHelper.choiceQAForType(context, arg.ty))
                        }
                    case _ =>
                        None
                }
            case _ =>
                None
        }
    }
}
