package com.github.woooking.cosyn.qa

import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object QAServer {
    implicit val timeout: Timeout = 1 minute

    def running(next: Long, mapping: Map[Long, ActorRef[QASessionMessage]]): Behavior[QAServerMessage] = Behaviors.receive { (context, message) =>
        implicit val scheduler: Scheduler = context.system.scheduler

        message match {
            case StartSession(ref, reqCtx, description) =>
                val session = context.spawn(QASession.initializing, next.toString)
                context.watchWith(session, SessionEnded(next))
                context.ask[Start, StartResponse](session)(r => Start(r, reqCtx, description)) {
                    case Success(m: QuestionFromSession) => Reply(ref, m)
                    case Success(m: Finished.type) => Reply(ref, m)
                    case Failure(_) => Reply(ref, Finished)
                }
                running(next + 1, mapping + (next -> session))
            case Answer(ref, id, answer) =>
                val session = mapping(id)
                context.ask[ProcessAnswer, ProcessAnswerResponse](session)(r => ProcessAnswer(r, answer)) {
                    case Success(m: QuestionFromSession) => Reply(ref, m)
                    case Success(m: ErrorAnswer) => Reply(ref, m)
                    case Success(m: Finished.type) => Reply(ref, m)
                    case Failure(_) => Reply(ref, Finished)
                }
                Behaviors.same
            case Undo(ref, id) =>
                val session = mapping(id)
                context.ask[ProcessUndo, ProcessUndoResponse](session)(ProcessUndo) {
                    case Success(m: QuestionFromSession) => Reply(ref, m)
                    case Success(m: CannotUndo.type) => Reply(ref, m)
                    case Failure(_) => Reply(ref, Finished)
                }
                Behaviors.same
            case SessionEnded(id) =>
                running(next, mapping - id)
            case Reply(ref, m) =>
                ref ! m
                Behaviors.same
        }
    }
}

