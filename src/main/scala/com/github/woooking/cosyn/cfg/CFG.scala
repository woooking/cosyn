package com.github.woooking.cosyn.cfg

import java.io.PrintStream

import com.github.woooking.cosyn.ir._
import com.github.woooking.cosyn.ir.statements.{IRStatement, IRAssignment}
import com.github.woooking.cosyn.util.Printable

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

class CFG extends Printable {
    private[this] var temp = 0
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer()
    val entry = new Statements
    val exit: Exit.type = Exit

    case class Context(block: Statements, break: Option[CFGBlock], continue: Option[CFGBlock])

    def createContext(b: Statements): Context = createContext(b, None, None)

    def createContext(b: Statements, br: Option[CFGBlock], c: Option[CFGBlock]): Context = Context(b, br, c)

    class IRPhi(val block: CFGBlock) extends IRStatement {
        block.phis += this

        val target: IRTemp = createTempVar()

        val operands: mutable.Set[IRVariable] = mutable.Set()

        def appendOperand(ope: IRVariable): Unit = {
            operands += ope

            ope match {
                case v: IRVariable => v.uses += this
                case _ =>
            }
        }

        override def uses: Seq[IRExpression] = operands.toSeq

        def replaceBy(variable: IRVariable): Unit = {
            block.phis -= this
            operands.foreach(_.uses -= this)
            target.replaced = Some(variable)
        }

        override def toString: String = s"$target=phi(${operands.mkString(", ")})"

        init()
    }

    class IRTemp extends IRVariable {
        private val _id: Int = temp
        var replaced: Option[IRVariable] = None
        var defStatement: IRStatement = _
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
        val id: Int = blocks.length
        blocks += this

        var isSealed = false

        val phis: ArrayBuffer[IRPhi] = ArrayBuffer()
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

        val statements: ArrayBuffer[IRStatement] = ArrayBuffer[IRStatement]()

        override def setNext(next: CFGBlock): Unit = {
            this.next = Some(next)
            next.preds += this
        }

        def addStatement(statement: IRStatement): Unit = statements += statement

        def optimize(): Unit = {
            statements.foreach {
                case s @ IRAssignment(target @ IRTemp(_), source @ IRTemp(_)) =>
                    target.replaced = Some(source)
                    statements -= s
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

    class Branch(condition: IRExpression, val thenBlock: CFGBlock, val elseBlock: CFGBlock) extends CFGBlock {
        thenBlock.preds += this
        elseBlock.preds += this

        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of branch")

        override def print(ps: PrintStream = System.out): Unit = {
            ps.println(this)
            ps.println(condition)
            ps.println(s"true -> $thenBlock")
            ps.println(s"false -> $elseBlock")
        }

        override def toString: String = s"[Block $id: Branch]"
    }

    object Exit extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of exit")

        override def print(ps: PrintStream = System.out): Unit = {
            ps.println(s"$this")
        }

        override def toString: String = s"[Block $id: Exit]"
    }

    class Switch extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of switch")

        override def print(ps: PrintStream = System.out): Unit = ???
    }

    override def print(ps: PrintStream = System.out): Unit = {
        blocks.foreach(_.print(ps))
    }

    def createTempVar(): IRTemp = new IRTemp

    def createStatements(): Statements = new Statements

    def createBranch(condition: IRExpression, thenBlock: CFGBlock, elseBlock: CFGBlock): Branch = new Branch(condition, thenBlock, elseBlock)

    def writeVar(name: String, block: CFGBlock, value: IRVariable): Unit = block.defs(name) = value

    def readVar(name: String, block: CFGBlock): IRVariable = block.defs.getOrElse(name, readVarRec(name, block))

    private def readVarRec(name: String, block: CFGBlock): IRVariable = {
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
