package com.github.woooking.cosyn.dfgprocessor.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.cfg.CFGSwitch.SwitchLabel
import com.github.woooking.cosyn.dfgprocessor.ir.IRExpression
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement

import scala.collection.mutable

class CFGSwitch(cfg: CFGImpl, selector: IRExpression) extends CFGBlock(cfg) {
    val blocks = mutable.Map[SwitchLabel, CFGBlock]()

    def update(label: SwitchLabel, block: CFGBlock) = blocks(label) = block

    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of switch")

    def print(ps: PrintStream = System.out): Unit = {
        ps.println(this)
        blocks.foreach {
            case (label, block) => ps.println(s"$label -> $block")
        }
    }

    override def toString: String = s"[Block $id: Switch]"

    override def statements: Seq[IRStatement] = phis
}

object CFGSwitch {
    sealed trait SwitchLabel

    case class ExpressionLabel(exprssion: IRExpression) extends SwitchLabel

    case object DefaultLabel extends SwitchLabel
}