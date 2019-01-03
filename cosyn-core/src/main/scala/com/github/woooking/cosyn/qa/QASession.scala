package com.github.woooking.cosyn.qa

import akka.actor.{ActorRef, FSM, Props}
import com.github.woooking.cosyn.code.model.HoleExpr
import com.github.woooking.cosyn.code.{Context, Pattern, Question}
import com.github.woooking.cosyn.config.Config
import com.github.woooking.cosyn.qa.QASession._

import scala.annotation.tailrec
import scala.collection.immutable.Stack
import scala.io.StdIn

class QASession(server: ActorRef) extends FSM[State, Data] {
    startWith(Running, Initialize)

    when(Running) {
        case Event(StartSession(ctx, description), Initialize) =>
            val pattern = Pattern.patterns(description)
            val newCtx = ctx.update(pattern)
            QuestionGenerator.generate(newCtx, pattern) match {
                case Some((hole, q)) =>
                    sender() ! (newCtx, pattern, q)
                    stay using SessionData(newCtx, pattern, hole, q, Stack())
                case None =>
                    stop
            }
        case Event(Answer(_, input), SessionData(context, pattern, hole, question, history)) =>
            question.processInput(context, pattern, hole, input) match {
                case Right((newCtx, newPattern)) =>
                    QuestionGenerator.generate(newCtx, newPattern) match {
                        case Some((hole, q)) =>
                            sender() ! (newCtx, newPattern, q)
                            stay using SessionData(newCtx, newPattern, hole, q, history.push((q, context, pattern)))
                        case None =>
                            stop
                    }
                case Left(q) =>
                    sender() ! (context, pattern, question)
                    stay using SessionData(context, pattern, hole, q, history.push((q, context, pattern)))
            }
    }

    initialize()
}

object QASession {

    sealed trait State

    case object Running extends State

    sealed trait Data

    case object Initialize extends Data

    case class SessionData(context: Context,
                           pattern: Pattern,
                           hole: HoleExpr,
                           question: Question,
                           history: Stack[(Question, Context, Pattern)]) extends Data

    def props(server: ActorRef): Props = Props(new QASession(server))
}