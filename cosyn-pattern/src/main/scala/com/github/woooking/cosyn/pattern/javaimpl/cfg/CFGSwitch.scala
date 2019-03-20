package com.github.woooking.cosyn.pattern.javaimpl.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFGSwitch.SwitchLabel
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRExpression
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.IRStatement

import scala.collection.mutable

class CFGSwitch(cfg: CFG, selector: IRExpression) extends CFGBlock(cfg) {
    val blocks: mutable.Map[SwitchLabel, CFGBlock] = mutable.Map[SwitchLabel, CFGBlock]()

    def update(label: SwitchLabel, block: CFGBlock): Unit = blocks(label) = block

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

    case class ExpressionLabel(expression: IRExpression) extends SwitchLabel

    case object DefaultLabel extends SwitchLabel
}