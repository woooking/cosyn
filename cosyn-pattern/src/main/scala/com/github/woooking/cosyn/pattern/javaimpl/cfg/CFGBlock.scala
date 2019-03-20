package com.github.woooking.cosyn.pattern.javaimpl.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.{IRPhi, IRStatement}
import com.github.woooking.cosyn.pattern.util.Printable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class CFGBlock(val cfg: CFG) {
    val defs: mutable.Map[String, IRExpression] = mutable.Map()
    val id: Int = cfg.blocks.length
    cfg.blocks += this

    var isSealed = false

    val phis: ArrayBuffer[IRPhi] = ArrayBuffer()
    val incompletePhis: mutable.Map[String, IRPhi] = mutable.Map()

    val preds: ArrayBuffer[CFGBlock] = ArrayBuffer()

    def seal(): Unit = {
        incompletePhis.foreach { case (name, p) =>
            cfg.addPhiOperands(name, p)
        }
        isSealed = true
    }

    def setNext(next: CFGBlock): Unit

    def statements: Seq[IRStatement]

}

object CFGBlock {
    implicit val cfgBlockPrintable: Printable[CFGBlock] = (b: CFGBlock, ps: PrintStream) => b match {
        case b: CFGStatements => b.print(ps)
        case b: CFGBranch => b.print(ps)
        case b: CFGSwitch => b.print(ps)
        case b: CFGExit => b.print(ps)
    }
}
