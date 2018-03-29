package com.github.woooking.cosyn.dfgprocessor.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.util.Printable

trait CFG {
    def blocks: Seq[CFGBlock]
    def entry: CFGBlock
    def exit: CFGExit
}

object CFG {
    implicit val cfgPrintable: Printable[CFG] = (obj: CFG, ps: PrintStream) => {
        val p = implicitly[Printable[CFGBlock]]
        obj.blocks.foreach(b => p.print(b, ps))
    }
}