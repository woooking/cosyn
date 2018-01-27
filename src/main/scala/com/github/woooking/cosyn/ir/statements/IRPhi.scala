package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.cfg.CFGBlock
import com.github.woooking.cosyn.ir.{IRTemp, IRExpression}

import scala.collection.mutable

class IRPhi(val block: CFGBlock) extends IRDefStatement(block.cfg) {
    block.phis += this

    val operands: mutable.Set[IRExpression] = mutable.Set()

    def appendOperand(ope: IRExpression): Unit = {
        operands += ope

        ope match {
            case v: IRExpression => v.uses += this
            case _ =>
        }
    }

    override def uses: Seq[IRExpression] = operands.toSeq

    def replaceBy(variable: IRExpression): Unit = {
        block.phis -= this
        operands.foreach(_.uses -= this)
        target.replaced = Some(variable)
    }

    override def toString: String = s"$target=phi(${operands.mkString(", ")})"

    init()
}