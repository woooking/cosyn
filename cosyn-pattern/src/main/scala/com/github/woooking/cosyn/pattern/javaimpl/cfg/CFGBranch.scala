package com.github.woooking.cosyn.pattern.javaimpl.cfg

import cats.Show
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.IRStatement

class CFGBranch(cfg: CFG, val condition: Option[IRExpression], val thenBlock: CFGBlock, val elseBlock: CFGBlock) extends CFGBlock(cfg) {
    thenBlock.preds += this
    elseBlock.preds += this

    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of branch")

    override def toString: String = s"[Block $id: Branch]"

    override def statements: Seq[IRStatement] = phis
}

object CFGBranch {
    implicit val cfgBranchShow: Show[CFGBranch] = (cfgBranch: CFGBranch) => {
        var s = cfgBranch.toString
        s += s"\n${cfgBranch.condition}"
        s += s"\ntrue -> ${cfgBranch.thenBlock}"
        s += s"\nelse -> ${cfgBranch.elseBlock}"
        s
    }
}