package com.github.woooking.cosyn.pattern.javaimpl.cfg

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.javaimpl.ir._
import com.github.woooking.cosyn.pattern.javaimpl.ir.statements.{IRPhi, IRStatement}
import com.github.woooking.cosyn.pattern.util.IDGenerator

import scala.collection.mutable.ArrayBuffer

class CFG(val name: String, val decl: Node) {

    private[this] val tempID = new IDGenerator
    val blocks: ArrayBuffer[CFGBlock] = ArrayBuffer()
    val entry = new CFGStatements(this)
    val exit: CFGExit = new CFGExit(this)

    case class Context(block: CFGStatements, break: List[CFGBlock], continue: List[CFGBlock]) {
        def change(b: CFGStatements): Context = copy(block = b)

        def push(b: CFGStatements, br: CFGBlock, c: CFGBlock): Context = Context(b, br :: break, c :: continue)

        def pop(b: CFGStatements): Context = Context(b, break.tail, continue.tail)
    }

    object Context {
        def apply(block: CFGStatements): Context = Context(block, Nil, Nil)
    }

    def createTempVar(definition: IRStatement): IRTemp = new IRTemp(tempID.next(), definition)

    def createStatements(): CFGStatements = new CFGStatements(this)

    def createBranch(condition: Option[IRExpression], thenBlock: CFGBlock, elseBlock: CFGBlock): CFGBranch = new CFGBranch(this, condition, thenBlock, elseBlock)

    def createSwitch(selector: IRExpression): CFGSwitch = new CFGSwitch(this, selector)

    def writeVar(name: String, block: CFGBlock, value: IRExpression): Unit = block.defs(name) = value

    def readVar(ty: String, name: String, block: CFGBlock): IRExpression = block.defs.getOrElse(name, readVarRec(ty, name, block))

    private def readVarRec(ty: String, name: String, block: CFGBlock): IRExpression = {
        val v = if (!block.isSealed) {
            val phi = new IRPhi(ty, block)
            block.incompletePhis(name) = phi
            phi.target
        } else if (block.preds.length == 1) {
            readVar(ty, name, block.preds(0))
        } else {
            val phi = new IRPhi(ty, block)
            writeVar(name, block, phi.target)
            addPhiOperands(name, phi)
        }
        writeVar(name, block, v)
        v
    }

    def addPhiOperands(name: String, phi: IRPhi): IRExpression = {
        phi.block.preds.foreach(pred => phi.appendOperand(readVar(phi.ty, name, pred)))
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
        val result = if (same == null) IRExtern(phi.ty, name) else same
        phi.replaceBy(result)
        (phi.target.uses - phi).foreach {
            case p: IRPhi => tryRemoveTrivialPhi(name, p)
            case _ =>
        }
        result
    }

    private def exprEquals(a: IRExpression, b: IRExpression): Boolean = {
        (a, b) match {
            case (t: IRTemp, _) if t.replaced.isDefined => exprEquals(t.replaced.get, b)
            case (_, t: IRTemp) if t.replaced.isDefined => exprEquals(a, t.replaced.get)
            case _ => a.equals(b)
        }
    }
}
