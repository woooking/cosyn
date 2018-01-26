package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFGBlock
import com.github.woooking.cosyn.ir.{IRExpression, IRTemp, IRVariable}

import scala.collection.mutable

class IRPhi(val block: CFGBlock) extends IRStatement {
    block.phis += this

    val target: IRTemp = block.cfg.createTempVar()

    val operands: mutable.Set[IRVariable] = mutable.Set()

    def appendOperand(ope: IRVariable): Unit = {
        operands += ope

        ope match {
            case v: IRVariable => v.uses += this
            case _ =>
        }
    }

    override def uses: Seq[IRExpression] = operands.toSeq

    def replaceBy(variable: IRVariable): Unit = {
        block.phis -= this
        operands.foreach(_.uses -= this)
        target.replaced = Some(variable)
    }

    override def toString: String = s"$target=phi(${operands.mkString(", ")})"

    init()
}