package com.github.woooking.cosyn.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.ir.statements.IRStatement
import de.parsemis.graph.Node

import scala.collection.mutable.ArrayBuffer

class CFGStatements(cfg: CFG) extends CFGBlock(cfg) {
    var next: Option[CFGBlock] = None

    val irStatements: ArrayBuffer[IRStatement] = ArrayBuffer[IRStatement]()

    override def setNext(next: CFGBlock): Unit = {
        this.next = Some(next)
        next.preds += this
    }

    def addStatement(statement: IRStatement): statement.type = {
        irStatements += statement
        statement
    }

    def optimize(): Unit = {
        irStatements.foreach {
            case _ =>
        }
    }

    override def print(ps: PrintStream = System.out): Unit = {
        ps.println(s"$this${next.map(" -> " + _).mkString}")
        phis.foreach(ps.println)
        irStatements.foreach(ps.println)
        ps.println()
    }

    override def toString: String = s"[Block $id: Statements]"

    override def statements: Seq[IRStatement] = phis ++ irStatements
}
