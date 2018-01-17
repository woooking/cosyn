package com.github.woooking.cosyn.javaparser.stmt

import com.github.javaparser.ast.stmt.{CatchClause, TryStmt => JPTryStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class TryStmt(override val delegate: JPTryStmt) extends Statement {
    val resources: List[Expression[_]] = delegate.getResources.asScala.map(r => Expression(r)).toList
    val tryBlock: BlockStmt = BlockStmt(delegate.getTryBlock)
    val catchClause: List[CatchClause] = delegate.getCatchClauses.asScala.toList
    val finallyBlock: Option[BlockStmt] = delegate.getFinallyBlock.asScala.map(BlockStmt.apply)
}

object TryStmt {
    def apply(delegate: JPTryStmt): TryStmt = new TryStmt(delegate)

    def unapply(arg: TryStmt): Option[(
        List[Expression[_]],
            BlockStmt,
            List[CatchClause],
            Option[BlockStmt]
        )] = Some((
        arg.resources,
        arg.tryBlock,
        arg.catchClause,
        arg.finallyBlock
    ))
}