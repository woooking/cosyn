package com.github.woooking.cosyn.pattern.javaimpl.cfg

import cats.Show
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.IRStatement

import scala.collection.mutable.ArrayBuffer

class CFGStatements(cfg: CFG) extends CFGBlock(cfg) {
    var next: Option[CFGBlock] = None

    val irStatements: ArrayBuffer[IRStatement] = ArrayBuffer[IRStatement]()

    def addStatement(statement: IRStatement): statement.type = {
        irStatements += statement
        statement
    }

    override def setNext(next: CFGBlock): Unit = {
        this.next = Some(next)
        next.preds += this
    }

    override def toString: String = s"[Block $id: Statements]"

    override def statements: Seq[IRStatement] = phis ++ irStatements
}

object CFGStatements {
    implicit val cfgStatementsShow: Show[CFGStatements] = (cfgStatements: CFGStatements) => {
        var s = cfgStatements.toString
        s += s"${cfgStatements.next.map(" -> " + _).mkString}"
        cfgStatements.phis.map(phi => s"\n$phi").foreach(s += _)
        cfgStatements.irStatements.map(stmt => s"\n$stmt").foreach(s += _)
        s
    }
}