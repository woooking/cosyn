package com.github.woooking.cosyn.javaparser.stmt

import cats.instances.list._
import cats.instances.option._
import com.github.javaparser.ast.stmt.{ForStmt => JPForStmt}
import com.github.woooking.cosyn.javaparser.expr.Expression
import com.github.woooking.cosyn.util.OptionConverters._

import scala.collection.JavaConverters._

class ForStmt(override val delegate: JPForStmt) extends Statement {
    val initialization: List[Expression[_]] = delegate.getInitialization.asScala.toList
    val compare: Option[Expression[_]] = delegate.getCompare.asScala
    val update: List[Expression[_]] = delegate.getUpdate.asScala.toList
    val body: Statement = Statement(delegate.getBody)
}

object ForStmt {
    def apply(delegate: JPForStmt): ForStmt = new ForStmt(delegate)

    def unapply(arg: ForStmt): Option[(
        List[Expression[_]],
            Option[Expression[_]],
            List[Expression[_]],
            Statement
        )] = Some((
        arg.initialization,
        arg.compare,
        arg.update,
        arg.body
    ))
}