package com.github.woooking.cosyn.pattern.javaimpl.ir.statements

import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFGBlock
import com.github.woooking.cosyn.pattern.javaimpl.ir.{IRTemp, IRExpression}

import scala.collection.mutable

class IRPhi(val ty: String, val block: CFGBlock) extends IRDefStatement(block.cfg) {
    block.phis += this

    val operands: mutable.ArrayBuffer[IRExpression] = mutable.ArrayBuffer()

    def appendOperand(ope: IRExpression): Unit = {
        operands += ope

        ope match {
            case v: IRExpression => v.uses += this
            case _ =>
        }
    }

    override def uses: Seq[IRExpression] = operands

    def replaceBy(variable: IRExpression): Unit = {
        block.phis -= this
        operands.foreach(_.uses -= this)
        target.replaced = Some(variable)
    }

    override def toString: String = s"$target=phi(${operands.mkString(", ")})"

    init()
}