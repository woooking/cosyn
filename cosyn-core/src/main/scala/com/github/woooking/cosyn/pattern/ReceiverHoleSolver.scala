package com.github.woooking.cosyn.pattern
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.util.CodeUtil

class ReceiverHoleSolver extends HoleResolver {
    private sealed trait MethodType

    private case class ConstructorType(ty: String) extends MethodType

    private case object OtherType extends MethodType

    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: MethodCallExpr if p.receiver.contains(hole) =>
                val vars = context.findVariables(p.receiverType)
                val producers = KnowledgeGraph.producers(context, p.receiverType)
                val cases = producers.groupBy {
                    case m if m.isConstructor => ConstructorType(m.getDeclareType.getQualifiedName)
                    case _ => OtherType
                }
                val methodChoices = cases.flatMap {
                    case (ConstructorType(ty), ms) => Seq(ConstructorChoice(ty, ms))
                    case (OtherType, m) =>
                        m.map(_.getQualifiedSignature).foreach(println)
                        Seq()
                }
                val simpleName = CodeUtil.qualifiedClassName2Simple(p.receiverType).toLowerCase
                val q = s"Which $simpleName?"
                Some(WhichQA(q, vars.map(VariableChoice.apply) ++ methodChoices))
            case _ =>
                None
        }
    }
}
