package com.github.woooking.cosyn.pattern.javaimpl.cfg

import cats.Show
import cats.implicits._
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.{IRPhi, IRStatement}

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
    implicit val cfgBlockShow: Show[CFGBlock] = {
        case b: CFGStatements => b.show
        case b: CFGBranch => b.show
        case b: CFGSwitch => b.show
        case b: CFGExit => b.show
    }
}
