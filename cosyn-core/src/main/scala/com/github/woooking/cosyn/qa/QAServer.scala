package com.github.woooking.cosyn.qa

import akka.actor.{ActorRef, FSM, Terminated}
import akka.util
import akka.util.Timeout
import com.github.woooking.cosyn.code.{Context, Pattern, Question}
import com.github.woooking.cosyn.qa.QAServer.{Running, State}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class QAServer extends FSM[State, (Long, Map[Long, ActorRef])] {
    import akka.pattern.{ask, pipe}
    implicit val timeout: util.Timeout = Timeout(1 minute)
    implicit val ec: ExecutionContext = context.dispatcher

    startWith(Running, 0L -> Map())

    when(Running) {
        case Event(m: StartSession, (next, mapping)) =>
            val session = context.actorOf(QASession.props(self), next.toString)
            val result = for {
                (ctx, pattern, question) <- (session ? m).mapTo[(Context, Pattern, Question)]
            } yield (ctx, pattern, next, question)
            result pipeTo sender()
            stay using (next + 1, mapping + (next -> session))
        case Event(m @ Answer(sessionId, _), (_, mapping)) =>
            val session = mapping(sessionId)
            (session ? m) pipeTo sender()
            stay
        case Event(Terminated(a), (next, mapping)) =>
            val id = a.path.name.toLong
            stay using (next, mapping - id)
    }

    initialize()
}

object QAServer {
    sealed trait State
    case object Running extends State

}