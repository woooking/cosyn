package com.github.woooking.cosyn.dfgprocessor.ir

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement
import com.github.woooking.cosyn.javaparser.NodeDelegate

import scala.collection.mutable
import scala.util.Try

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

abstract class IRExpression(val fromNode: Set[NodeDelegate[_]]) {
    val uses: mutable.Set[IRStatement] = mutable.Set()

    def definition: Option[IRStatement] = None
}

class IRTemp(initID: Int, defi: IRStatement) extends IRExpression(Set.empty) {
    var replaced: Option[IRExpression] = None

    override def definition: Option[IRStatement] = replaced match {
        case None => Some(defi)
        case Some(t) => t.definition
    }

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
    def unapply(arg: IRTemp): Option[Int] = Try {arg.id}.toOption
}

case object IRUndef extends IRExpression(Set.empty) {
}

case class IRExtern(name: String) extends IRExpression(Set.empty) {
    override def toString: String = name
}

case class IRArg(name: String, ty: Type) extends IRExpression(Set.empty) {
    override def toString: String = name
}

sealed case class IRBoolean(value: Boolean) extends IRExpression(Set.empty) {
    override def toString: String = s"$value"
}

case class IRArray(values: List[IRExpression]) extends IRExpression(Set.empty)

case class IRChar(value: Char) extends IRExpression(Set.empty)

case class IRString(value: String) extends IRExpression(Set.empty) {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int, from: Set[NodeDelegate[_]]) extends IRExpression(from) {
    override def toString: String = s"$value"
}

case class IRDouble(value: Double) extends IRExpression(Set.empty) {
    override def toString: String = s"$value"
}

case class IRLong(value: Long) extends IRExpression(Set.empty) {
    override def toString: String = s"$value"
}

case object IRNull extends IRExpression(Set.empty) {
    override def toString: String = "null"
}

case object IRThis extends IRExpression(Set.empty)

case object IRSuper extends IRExpression(Set.empty)

case object IRLambda extends IRExpression(Set.empty)

case object IRMethodReference extends IRExpression(Set.empty)

case class IRTypeObject(ty: Type) extends IRExpression(Set.empty) {
    override def toString: String = ty.asString()
}

