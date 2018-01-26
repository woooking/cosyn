package com.github.woooking.cosyn.cfg

import com.github.woooking.cosyn.ir.IRVariable
import com.github.woooking.cosyn.ir.statements.IRPhi
import com.github.woooking.cosyn.util.Printable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class CFGBlock(val cfg: CFG) extends Printable {
    val defs: mutable.Map[String, IRVariable] = mutable.Map()
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
}
