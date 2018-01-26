package com.github.woooking.cosyn.ir

import com.github.javaparser.ast.`type`.Type
import com.github.woooking.cosyn.cfg.{CFG, CFGBlock}
import com.github.woooking.cosyn.ir.statements.IRStatement

import scala.collection.mutable
import scala.util.Try

sealed trait IRExpression extends NodeResult

object IRExpression {
    val True = IRBoolean(true)

    val False = IRBoolean(false)
}

trait IRVariable extends IRExpression {
    val uses: mutable.Set[IRStatement] = mutable.Set()
}

class IRTemp(initID: Int) extends IRVariable {
    var replaced: Option[IRVariable] = None
    var defStatement: IRStatement = _

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

case object IRUndef extends IRVariable

case class IRExtern(name: String) extends IRVariable {
    override def toString: String = name
}

case class IRArg(name: String, ty: Type) extends IRVariable {
    override def toString: String = name
}

sealed case class IRBoolean(value: Boolean) extends IRExpression {
    override def toString: String = s"$value"
}

case class IRChar(value: Char) extends IRExpression

case class IRString(value: String) extends IRExpression {
    override def toString: String = s""""$value""""
}

case class IRInteger(value: Int) extends IRExpression {
    override def toString: String = s"$value"
}

case object IRNull extends IRVariable {
    override def toString: String = "null"
}

case object IRThis extends IRVariable

case class IRTypeObject(ty: Type) extends IRVariable {
    override def toString: String = ty.asString()
}

