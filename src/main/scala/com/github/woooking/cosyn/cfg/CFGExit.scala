package com.github.woooking.cosyn.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.statements.IRStatement
import de.parsemis.graph.Node

class CFGExit(cfg: CFG) extends CFGBlock(cfg) {
    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of exit")

    override def print(ps: PrintStream = System.out): Unit = {
        ps.println(s"$this")
    }

    override def toString: String = s"[Block $id: Exit]"

    override def statements: Seq[IRStatement] = phis
}
