package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression

class IRMethodInvocation(cfg: CFG, val ty: String, val name: String, val receiver: Option[IRExpression], val args: Seq[IRExpression], fromNode: Set[Node]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=${receiver.map(_ + ".").getOrElse("")}$name(${args.mkString(", ")})"

    override def uses: Seq[IRExpression] = receiver.toSeq ++ args

    init()
}

object IRMethodInvocation {
    def apply(cfg: CFG, ty: String, name: String, receiver: Option[IRExpression], args: Seq[IRExpression], fromNode: Set[Node]): IRMethodInvocation =
        new IRMethodInvocation(cfg, ty, name, receiver, args, fromNode)
}