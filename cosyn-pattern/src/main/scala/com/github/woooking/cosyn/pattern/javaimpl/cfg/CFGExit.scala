package com.github.woooking.cosyn.pattern.javaimpl.cfg

import java.io.PrintStream

import cats.Show
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.IRStatement

class CFGExit(cfg: CFG) extends CFGBlock(cfg) {
    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of exit")

    def print(ps: PrintStream = System.out): Unit = {
        ps.println(s"$this")
    }

    override def toString: String = s"[Block $id: Exit]"

    override def statements: Seq[IRStatement] = phis
}

object CFGExit {
    implicit val cfgExitShow: Show[CFGExit] = (cfgExit: CFGExit) => {
        cfgExit.toString
    }
}