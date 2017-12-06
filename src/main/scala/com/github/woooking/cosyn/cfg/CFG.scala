package com.github.woooking.cosyn.cfg

import com.github.woooking.cosyn.ir.{IRExpression, IRUndef, IRVariable}
import com.github.woooking.cosyn.ir.statements.IRAbstractStatement

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class CFG {
    var num = 0
    var temp = 0
    var phi = 0
    val entry: Entry.type = Entry
    val exit: Exit.type = Exit
    val defs: mutable.Map[String, mutable.Map[CFGBlock, IRExpression]] = mutable.Map()
    val incompletePhis: mutable.Map[CFGBlock, mutable.Map[String, IRPhi]] = mutable.Map()
    val sealedBlocks: ArrayBuffer[CFGBlock] = ArrayBuffer()

    case class IRPhi(block: CFGBlock) extends IRVariable {
        val id: Int = phi
        phi += 1

        val operands: ArrayBuffer[IRExpression] = ArrayBuffer()
        val users: ArrayBuffer[IRExpression] = ArrayBuffer()

        def appendOperand(ope: IRExpression): Unit = operands += ope

    }

    abstract class CFGBlock {
        val id: Int = num
        num += 1

        val preds: ArrayBuffer[CFGBlock] = ArrayBuffer[CFGBlock]()

        def setNext(next: CFGBlock)
    }

    class Statements extends CFGBlock {
        var next: Option[CFGBlock] = None

        val statements: ArrayBuffer[IRAbstractStatement] = ArrayBuffer[IRAbstractStatement]()

        override def setNext(next: CFGBlock): Unit = {
            this.next = Some(next)
            next.preds += this
        }

        def addStatement(statement: IRAbstractStatement): Unit = statements += statement
    }

    class Branch(condition: IRVariable, thenBlock: CFGBlock, elseBlock: CFGBlock) extends CFGBlock {
        thenBlock.preds += this
        elseBlock.preds += this

        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of branch")
    }

    private object Entry extends CFGBlock {
        var next: Option[CFGBlock] = None

        override def setNext(next: CFGBlock): Unit = {
            this.next = Some(next)
            next.preds += this
        }
    }

    private object Exit extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of exit")
    }

    class Switch extends CFGBlock {
        override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of switch")
    }

    def writeVar(name: String, block: CFGBlock, value: IRExpression): Unit = {
        defs.getOrElseUpdate(name, mutable.Map())(block) = value
    }

    def readVar(name: String, block: CFGBlock): IRExpression = defs.get(name) match {
        case None => readVarRec(name, block)
        case Some(v) => v.getOrElse(block, readVarRec(name, block))
    }


    def readVarRec(name: String, block: CFGBlock): IRExpression = {
        val v = if (!sealedBlocks.contains(block)) {
            val phi = IRPhi(block)
            incompletePhis.getOrElseUpdate(block, mutable.Map())(name) = phi
            phi
        } else if (block.preds.length == 1) {
            readVar(name, block.preds(0))
        } else {
            val phi = IRPhi(block)
            writeVar(name, block, phi)
            addPhiOperands(name, phi)
        }
        writeVar(name, block, v)
        v
    }

    def addPhiOperands(name: String, phi: IRPhi): IRExpression = {
        phi.block.preds.foreach(pred => phi.appendOperand(readVar(name, pred)))
        tryRemoveTrivialPhi(phi)
    }

    def tryRemoveTrivialPhi(phi: IRPhi): IRExpression = {
        phi.operands.distinct match {
            case opes if opes.length >= 2 => phi
            case opes =>
                val same = if (opes.isEmpty) IRUndef else opes(0)
                val users = phi.users - phi
                users.filter(_.isInstanceOf[IRPhi]).map(p => tryRemoveTrivialPhi(p.asInstanceOf[IRPhi]))
                same
        }
    }

    def sealBlock(block: CFGBlock): Unit = {
        incompletePhis(block).foreach { case (name, p) =>
            addPhiOperands(name, p)
        }
        sealedBlocks += block
    }
}
