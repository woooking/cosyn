package com.github.woooking.cosyn.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.ir.statements.{IRStatement}

import scala.collection.mutable.ArrayBuffer

class CFGStatements(cfg: CFG) extends CFGBlock(cfg) {
    var next: Option[CFGBlock] = None

    val statements: ArrayBuffer[IRStatement] = ArrayBuffer[IRStatement]()

    override def setNext(next: CFGBlock): Unit = {
        this.next = Some(next)
        next.preds += this
    }

    def addStatement(statement: IRStatement): statement.type = {
        statements += statement
        statement
    }

    def optimize(): Unit = {
        statements.foreach {
            case _ =>
        }
    }

    override def print(ps: PrintStream = System.out): Unit = {
        ps.println(s"$this${next.map(" -> " + _).mkString}")
        phis.foreach(ps.println)
        statements.foreach(ps.println)
        ps.println()
    }

    override def toString: String = s"[Block $id: Statements]"
}
