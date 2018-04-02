package com.github.woooking.cosyn.dfgprocessor.ir

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.dfgprocessor.ir.statements.IRStatement
import com.github.woooking.cosyn.javaparser.NodeDelegate

import scala.collection.mutable
import scala.util.Try

abstract class IRExpression(val fromNodes: Set[NodeDelegate[_]]) {
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

sealed case class IRBoolean(value: Boolean, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRArray(values: List[IRExpression], fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode))

case class IRChar(value: Char, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode))

case class IRString(value: String, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRDouble(value: Double, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRLong(value: Long, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRNull(fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = "null"
}

case class IRThis(fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode))

case class IRSuper(fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode))

case object IRLambda extends IRExpression(Set.empty)

case object IRMethodReference extends IRExpression(Set.empty)

case class IRTypeObject(ty: Type, fromNode: NodeDelegate[_]) extends IRExpression(Set(fromNode)) {
    override def toString: String = ty.asString()
}

