package com.github.woooking.cosyn.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.ir._
import com.github.woooking.cosyn.ir.statements.IRPhi
import com.github.woooking.cosyn.util.{IDGenerator, Printable}

import scala.collection.mutable.ArrayBuffer

class CFG extends Printable {
    private[this] val tempID = new IDGenerator
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer()
    val entry = new CFGStatements(this)
    val exit: Exit = new Exit(this)

    case class Context(block: CFGStatements, break: Option[CFGBlock], continue: Option[CFGBlock])

    def createContext(b: CFGStatements): Context = createContext(b, None, None)

    def createContext(b: CFGStatements, br: Option[CFGBlock], c: Option[CFGBlock]): Context = Context(b, br, c)

    override def print(ps: PrintStream = System.out): Unit = {
        blocks.foreach(_.print(ps))
    }

    def createTempVar(): IRTemp = new IRTemp(tempID.next())

    def createStatements(): CFGStatements = new CFGStatements(this)

    def createBranch(condition: IRExpression, thenBlock: CFGBlock, elseBlock: CFGBlock): Branch = new Branch(this, condition, thenBlock, elseBlock)

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
        val others = phi.operands.filter {
            case IRTemp(id) if id == phi.target.id => false
            case _ => true
        }
        if (others.size >= 2) phi.target
        else {
            val same = if (others.isEmpty) IRExtern(name) else others.last
            phi.replaceBy(same)
            (phi.target.uses - phi).foreach {
                case p: IRPhi => tryRemoveTrivialPhi(name, p)
                case _ =>
            }
            same
        }
    }

    def optimize(): Unit = {
        blocks.foreach {
            case statements: CFGStatements => statements.optimize()
            case _ =>
        }
    }
}
