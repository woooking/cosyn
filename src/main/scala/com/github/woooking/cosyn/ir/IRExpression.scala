package com.github.woooking.cosyn.ir

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.ir.statements.IRStatement

import scala.collection.mutable
import scala.util.Try

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

abstract class IRExpression(val definition: Option[IRStatement]) {
    val uses: mutable.Set[IRStatement] = mutable.Set()
}

class IRTemp(initID: Int, definition: IRStatement) extends IRExpression(Some(definition)) {
    var replaced: Option[IRExpression] = None

    def id: Int = replaced match {
        case None => initID
        case Some(t: IRTemp) => t.id
        case _ =>
            throw new RuntimeException("could not get id of a replaced temp")
    }

    override def toString: String = replaced match {
        case None => s"#$initID"
        case Some(r) => r.toString
    }
}

object IRTemp {
    def unapply(arg: IRTemp): Option[Int] = Try { arg.id }.toOption
}

case object IRUndef extends IRExpression(None)

case class IRExtern(name: String) extends IRExpression(None) {
    override def toString: String = name
}

case class IRArg(name: String, ty: Type) extends IRExpression(None) {
    override def toString: String = name
}

sealed case class IRBoolean(value: Boolean) extends IRExpression(None) {
    override def toString: String = s"$value"
}

case class IRChar(value: Char) extends IRExpression(None)

case class IRString(value: String) extends IRExpression(None) {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int) extends IRExpression(None) {
    override def toString: String = s"$value"
}

case object IRNull extends IRExpression(None) {
    override def toString: String = "null"
}

case object IRThis extends IRExpression(None)

case class IRTypeObject(ty: Type) extends IRExpression(None) {
    override def toString: String = ty.asString()
}

