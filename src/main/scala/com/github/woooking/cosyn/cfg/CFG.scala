package com.github.woooking.cosyn.cfg

import com.github.woooking.cosyn.ir._
import com.github.woooking.cosyn.ir.statements.{IRAbstractStatement, IRAssignment}
import com.github.woooking.cosyn.util.Printable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class CFG extends Printable {
    var num = 0
    var temp = 0
    var phi = 0
    val entry = new Statements
    val exit: Exit.type = Exit
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer(entry, exit)
    val defs: mutable.Map[String, mutable.Map[CFGBlock, IRVariable]] = mutable.Map()

    trait Context {
        val block: Statements
        val break: Option[CFGBlock]
        val continue: Option[CFGBlock]
    }

    def createContext(b: Statements): Context = createContext(b, None, None)

    def createContext(b: Statements, br: Option[CFGBlock], c: Option[CFGBlock]): Context = new Context {
        override val block: Statements = b
        override val break: Option[CFGBlock] = br
        override val continue: Option[CFGBlock] = c
    }

    class IRPhi(val block: CFGBlock) extends IRAbstractStatement {
        val id: Int = phi
        phi += 1

        block.phis += this

        val target: IRTemp = createTempVar()

        val operands: mutable.Set[IRVariable] = mutable.Set()
        val users: ArrayBuffer[IRExpression] = ArrayBuffer()

        def appendOperand(ope: IRVariable): Unit = operands += ope

        def replaceBy(variable: IRVariable): Unit = {
            val index = block.phis.indexOf(this)
            block.phis(index) = IRAssignment(target, variable)
        }

        override def toString: String = s"$target=phi(${operands.mkString(", ")})"
    }

    class IRTemp extends IRVariable {
        val id: Int = temp
        temp += 1

        override def toString: String = s"#$id"
    }

    abstract class CFGBlock extends Printable {
        val id: Int = num
        num += 1

        var isSealed = false

        val phis: ArrayBuffer[IRAbstractStatement] = ArrayBuffer()
        val incompletePhis: mutable.Map[String, IRPhi] = mutable.Map()

        val preds: ArrayBuffer[CFGBlock] = ArrayBuffer()

        def seal(): Unit = {
            incompletePhis.foreach { case (name, p) =>
                addPhiOperands(name, p)
            }
            isSealed = true
        }

        def setNext(next: CFGBlock): Unit
    }

    class Statements extends CFGBlock {
        var next: Option[CFGBlock] = None

        val statements: ArrayBuffer[IRAbstractStatement] = ArrayBuffer[IRAbstractStatement]()

        override def setNext(next: CFGBlock): Unit = {
            this.next = Some(next)
            next.preds += this
        }

        def addStatement(statement: IRAbstractStatement): Unit = statements += statement

        override def print(): Unit = {
            println(s"$this${next.map(" -> " + _).mkString}")
            phis.foreach(println)
            statements.foreach(println)
            println()
        }

        override def toString: String = s"[Block $id: Statements]"
    }

    class Branch(condition: IRExpression, thenBlock: CFGBlock, elseBlock: CFGBlock) extends CFGBlock {
        thenBlock.preds += this
        elseBlock.preds += this

        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of branch")

        override def print(): Unit = {
            println(this)
            println(condition)
            println(s"true -> $thenBlock")
            println(s"false -> $elseBlock")
        }

        override def toString: String = s"[Block $id: Branch]"
    }

    object Exit extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of exit")

        override def print(): Unit = {
            println(s"$this")
        }

        override def toString: String = s"[Block $id: Exit]"
    }

    class Switch extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of switch")

        override def print(): Unit = ???
    }

    override def print(): Unit = {
        blocks.foreach(_.print())
    }

    def createTempVar(): IRTemp = new IRTemp

    def createStatements(): Statements = {
        val block = new Statements
        blocks += block
        block
    }

    def createBranch(condition: IRExpression, thenBlock: CFGBlock, elseBlock: CFGBlock): Branch = {
        val block = new Branch(condition, thenBlock, elseBlock)
        blocks += block
        block
    }

    def writeVar(name: String, block: CFGBlock, value: IRVariable): Unit = {
        defs.getOrElseUpdate(name, mutable.Map())(block) = value
    }

    def readVar(name: String, block: CFGBlock): IRVariable = defs.get(name) match {
        case None => readVarRec(name, block)
        case Some(v) => v.getOrElse(block, readVarRec(name, block))
    }

    def readVarRec(name: String, block: CFGBlock): IRVariable = {
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

    def addPhiOperands(name: String, phi: IRPhi): IRVariable = {
        phi.block.preds.foreach(pred => phi.appendOperand(readVar(name, pred)))
        tryRemoveTrivialPhi(phi)
    }

    def tryRemoveTrivialPhi(phi: IRPhi): IRVariable = {
        if (phi.operands.size >= 2) phi.target
        else {
            val same = if (phi.operands.isEmpty) IRUndef else phi.operands.last
            phi.replaceBy(same)
            same
        }
    }

}
