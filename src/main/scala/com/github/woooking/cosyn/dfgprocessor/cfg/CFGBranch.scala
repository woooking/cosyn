package com.github.woooking.cosyn.dfgprocessor.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement
import de.parsemis.graph.Node

class CFGBranch(cfg: CFG, condition: IRExpression, val thenBlock: CFGBlock, val elseBlock: CFGBlock) extends CFGBlock(cfg) {
    thenBlock.preds += this
    elseBlock.preds += this

    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of branch")

    override def print(ps: PrintStream = System.out): Unit = {
        ps.println(this)
        ps.println(condition)
        ps.println(s"true -> $thenBlock")
        ps.println(s"false -> $elseBlock")
    }

    override def toString: String = s"[Block $id: Branch]"

    override def statements: Seq[IRStatement] = phis
}
