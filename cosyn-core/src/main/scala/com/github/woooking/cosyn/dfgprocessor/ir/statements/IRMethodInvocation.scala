package com.github.woooking.cosyn.dfgprocessor.ir.statements

import com.github.woooking.cosyn.dfgprocessor.cfg.CFG
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.javaparser.NodeDelegate

class IRMethodInvocation(cfg: CFG, val name: String, val receiver: Option[IRExpression], val args: Seq[IRExpression], fromNode: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNode) {
    override def toString: String = s"$target=${receiver.map(_ + ".").getOrElse("")}$name(${args.mkString(", ")})"

    override def uses: Seq[IRExpression] = receiver.toSeq ++ args

    init()
}

