package com.github.woooking.cosyn.cfg

import com.github.woooking.cosyn.ir._
import com.github.woooking.cosyn.ir.statements.{IRAbstractStatement, IRAssignment}
import com.github.woooking.cosyn.util.Printable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

class CFG extends Printable {
    var num = 0
    var temp = 0
    var phi = 0
    val entry = new Statements
    val exit: Exit.type = Exit
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer(entry, exit)

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

        def appendOperand(ope: IRVariable): Unit = {
            operands += ope
            addUse(ope)
        }

        def replaceBy(variable: IRVariable): Unit = {
            block.phis -= this
            operands.foreach(_.uses -= this)
            target.replaced = Some(variable)
        }

        override def toString: String = s"$target=phi(${operands.mkString(", ")})"
    }

    class IRTemp extends IRVariable {
        private val _id: Int = temp
        var replaced: Option[IRVariable] = None
        temp += 1

        def id: Int = replaced match {
            case None => _id
            case Some(t: IRTemp) => t.id
            case _ =>
                throw new RuntimeException("could not get id of a replaced temp")
        }

        override def toString: String = replaced match {
            case None => s"#${_id}"
            case Some(r) => r.toString
        }
    }

    object IRTemp {
        def unapply(arg: IRTemp): Option[Int] = Try { arg.id }.toOption
    }

    abstract class CFGBlock extends Printable {
        val defs: mutable.Map[String, IRVariable] = mutable.Map()
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

        def optimize(): Unit = {
            statements.foreach {
                case s @ IRAssignment(target @ IRTemp(_), source @ IRTemp(_)) =>
                    target.replaced = Some(source)
                    statements -= s
                case _ =>
            }
        }

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
        block.defs(name) = value
    }

    def readVar(name: String, block: CFGBlock): IRVariable = block.defs.getOrElse(name, readVarRec(name, block))

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
        tryRemoveTrivialPhi(name, phi)
    }

    def tryRemoveTrivialPhi(name: String, phi: IRPhi): IRVariable = {
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
            case statements: Statements => statements.optimize()
            case _ =>
        }
    }
}
