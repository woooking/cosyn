package com.github.woooking.cosyn.ir.statements

import com.github.woooking.cosyn.ir.{IRExpression, IRVariable}

case class IRMethodInvocation(target: IRVariable, name: String, receiver: Option[IRExpression], args: Seq[IRExpression]) extends IRAbstractStatement {
    override def toString: String = s"$target=${receiver.map(_ + ".").getOrElse("")}$name(${args.mkString(", ")})"
    receiver.foreach(addUse)
    args.foreach(addUse)
}

