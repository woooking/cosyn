package com.github.woooking.cosyn.dfgprocessor.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode}
import com.github.woooking.cosyn.dfgprocessor.ir._
import com.github.woooking.cosyn.dfgprocessor.ir.statements.{IRPhi, IRStatement}
import com.github.woooking.cosyn.javaparser.NodeDelegate
import com.github.woooking.cosyn.javaparser.body.BodyDeclaration
import com.github.woooking.cosyn.util.{IDGenerator, Printable}
import de.parsemis.graph.Node

import scala.collection.mutable.ArrayBuffer

class CFG(val file: String, val name: String, val decl: BodyDeclaration[_]) extends Printable {
    type DNode = Node[DFGNode, DFGEdge]

    private[this] val tempID = new IDGenerator
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer()
    val entry = new CFGStatements(this)
    val exit: CFGExit = new CFGExit(this)

    case class Context(block: CFGStatements, break: Option[CFGBlock], continue: Option[CFGBlock])

    def createContext(b: CFGStatements): Context = createContext(b, None, None)

    def createContext(b: CFGStatements, br: Option[CFGBlock], c: Option[CFGBlock]): Context = Context(b, br, c)

    override def print(ps: PrintStream = System.out): Unit = {
        blocks.foreach(_.print(ps))
    }

    def createTempVar(definition: IRStatement): IRTemp = new IRTemp(tempID.next(), definition)

    def createStatements(): CFGStatements = new CFGStatements(this)

    def createBranch(condition: IRExpression, thenBlock: CFGBlock, elseBlock: CFGBlock): CFGBranch = new CFGBranch(this, condition, thenBlock, elseBlock)

    def createSwitch(selector: IRExpression): CFGSwitch = new CFGSwitch(this, selector)

    def writeVar(name: String, block: CFGBlock, value: IRExpression): Unit = block.defs(name) = value

    def readVar(name: String, block: CFGBlock): IRExpression = block.defs.getOrElse(name, readVarRec(name, block))

    private def readVarRec(name: String, block: CFGBlock): IRExpression = {
        val v = if (!block.isSealed) {
            val phi = new IRPhi(block)
            block.incompletePhis(name) = phi
            phi.target
        } else if (block.preds.length == 1) {
            readVar(name, block.preds(0))
        } else {
            val phi = new IRPhi(block)
            writeVar(name, block, phi.target)
            addPhiOperands(name, phi)
        }
        writeVar(name, block, v)
        v
    }

    def addPhiOperands(name: String, phi: IRPhi): IRExpression = {
        phi.block.preds.foreach(pred => phi.appendOperand(readVar(name, pred)))
        tryRemoveTrivialPhi(name, phi)
    }

    def tryRemoveTrivialPhi(name: String, phi: IRPhi): IRExpression = {
        var same: IRExpression = null
        for (op <- phi.operands) {
            if (!exprEquals(op, same) && !exprEquals(op, phi.target)) {
                if (same != null) return phi.target
                same = op
            }
        }
        val result = if (same == null) IRExtern(name) else same
        phi.replaceBy(result)
        (phi.target.uses - phi).foreach {
            case p: IRPhi => tryRemoveTrivialPhi(name, p)
            case _ =>
        }
        result
    }

    def optimize(): Unit = {
        blocks.foreach {
            case statements: CFGStatements => statements.optimize()
            case _ =>
        }
    }

    private def exprEquals(a: IRExpression, b: IRExpression): Boolean = {
        (a, b) match {
            case (t: IRTemp, _) if t.replaced.isDefined => exprEquals(t.replaced.get, b)
            case (_, t: IRTemp) if t.replaced.isDefined => exprEquals(a, t.replaced.get)
            case _ => a.equals(b)
        }
    }
}
