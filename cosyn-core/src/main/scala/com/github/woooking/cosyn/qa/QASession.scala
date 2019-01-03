package com.github.woooking.cosyn.qa

import akka.actor.{ActorRef, FSM, Props}
import com.github.woooking.cosyn.code.{Context, Pattern, Question}
import com.github.woooking.cosyn.qa.QASession._

import scala.collection.immutable.Stack

class QASession(server: ActorRef) extends FSM[State, Data] {
    startWith(Running, Initialize)

    when(Running) {
        case Event(StartSession(ctx, description), Initialize) =>
            val pattern = Pattern.patterns(description)
            val newCtx = ctx.update(pattern)
            val question = QuestionGenerator.generate(newCtx, pattern)
            sender() ! (newCtx, pattern, question)
            stay using SessionData(newCtx, pattern, question, Stack())
        case Event(Answer(_, input), SessionData(context, pattern, question, history)) =>
    }

    initialize()
}

object QASession {

    sealed trait State

    case object Running extends State

    sealed trait Data

    case object Initialize extends Data

    case class SessionData(context: Context, pattern: Pattern, question: Question, history: Stack[Pattern]) extends Data

    def props(server: ActorRef): Props = Props(new QASession(server))
}