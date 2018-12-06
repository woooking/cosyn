package com.github.woooking.cosyn.pattern.hole_resolver

import com.github.javaparser.JavaParser
import com.github.woooking.cosyn.knowledge_graph.{JavadocUtil, KnowledgeGraph, NLP}
import com.github.woooking.cosyn.pattern._
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil


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
                        if (CodeUtil.isPrimitive(arg.ty) || arg.ty == "java.lang.String") {
                            Some(PrimitiveQA(
                                KnowledgeGraph.getMethodJavadoc(p.getQualifiedSignature).map(JavadocUtil.extractParamInfoFromJavadoc(index)),
                                arg.ty
                            ))
                        } else {
                            val producers = KnowledgeGraph.producers(context, arg.ty)
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
                            val simpleName = CodeUtil.qualifiedClassName2Simple(arg.ty).toLowerCase
                            val q = s"Which $simpleName?"
                            Some(ChoiceQA(q, vars.map(VariableChoice.apply) ++ methodChoices))
                        }
                    case _ =>
                        None
                }
            case _ =>
                None
        }
    }
}
