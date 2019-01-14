package com.github.woooking.cosyn.pattern.dfgprocessor.ir

import com.github.javaparser.ast.Node
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.statements.IRStatement

import scala.collection.mutable
import scala.util.Try

abstract class IRExpression(val fromNodes: Set[Node]) {
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
    def unapply(arg: IRTemp): Option[Int] = Try {
        arg.id
    }.toOption
}

case class IREnum(ty: String, value: String) extends IRExpression(Set.empty) {
    override def toString: String = s"$ty.$value"
}

case object IRUndef extends IRExpression(Set.empty) {
}

case class IRExtern(name: String) extends IRExpression(Set.empty) {
    override def toString: String = name
}

case class IRArg(name: String, ty: String) extends IRExpression(Set.empty) {
    override def toString: String = name
}

sealed case class IRBoolean(value: Boolean, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRArray(values: List[IRExpression], fromNode: Node) extends IRExpression(Set(fromNode))

case class IRChar(value: Char, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"'$value'"
}

case class IRString(value: String, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRDouble(value: Double, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRLong(value: Long, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = s"$value"
}

case class IRNull(fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = "null"
}

case class IRThis(fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = "this"
}

case class IRSuper(fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = "super"
}

case object IRLambda extends IRExpression(Set.empty)

case object IRMethodReference extends IRExpression(Set.empty)

case class IRTypeObject(ty: String, fromNode: Node) extends IRExpression(Set(fromNode)) {
    override def toString: String = ty
}
