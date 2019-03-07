package com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRExpression

class IRMethodInvocation(cfg: CFG, val name: String, val receiver: Option[IRExpression], val args: Seq[IRExpression], fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=${receiver.map(_ + ".").getOrElse("")}$name(${args.mkString(", ")})"

    override def uses: Seq[IRExpression] = receiver.toSeq ++ args

    init()
}

object IRMethodInvocation {
    def apply(cfg: CFG, name: String, receiver: Option[IRExpression], args: Seq[IRExpression], fromNode: Set[Node]): IRMethodInvocation =
        new IRMethodInvocation(cfg, name, receiver, args, fromNode)
}