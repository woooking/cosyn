package com.github.woooking.cosyn.cfg

import com.github.woooking.cosyn.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.IRExpression
import com.github.woooking.cosyn.ir.statements.{IRPhi, IRStatement}
import com.github.woooking.cosyn.util.Printable
import de.parsemis.graph.Node

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class CFGBlock(val cfg: CFG) extends Printable {
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
