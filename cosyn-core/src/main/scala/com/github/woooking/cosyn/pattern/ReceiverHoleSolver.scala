package com.github.woooking.cosyn.pattern
import com.github.woooking.cosyn.pattern.model.expr.{HoleExpr, MethodCallExpr}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt

class ReceiverHoleSolver extends HoleResolver {
    override def resolve(ast: BlockStmt, hole: HoleExpr, context: Context): Option[QA] = {
        hole.parent match {
            case p: MethodCallExpr if p.receiver.contains(hole) =>
                val vars = context.findVariables(p.receiverType)
                println(vars)
                ???
            case _ =>
                None
        }
    }
}
