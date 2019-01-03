package com.github.woooking.cosyn.qa

import akka.actor.Scheduler
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object QAServer {
    implicit val timeout: Timeout = 1 minute

    def running(next: Long, mapping: Map[Long, ActorRef[QASessionMessage]]): Behavior[QAServerMessage] = Behaviors.receive { (context, message) =>
        implicit val schedular: Scheduler = context.system.scheduler

        message match {
            case StartSessionRequest(ref, reqCtx, description) =>
                val session = context.spawn(QASession.initializing, next.toString)
                context.watchWith(session, SessionEnded(next))
                session ? ((r: ActorRef[NextQuestion]) => Start(r, reqCtx, description)) onComplete {
                    case Success(QuestionFromSession(ctx, pattern, q)) => ref ! StartSessionResponseWithQuestion(next, ctx, pattern, q)
                    case Success(Finished) => ref ! Finished
                    case Failure(_) => ref ! Finished
                }
                running(next + 1, mapping + (next -> session))
            case AnswerRequest(ref, sessionId, answer) =>
                val session = mapping(sessionId)
                session ? ((r: ActorRef[NextQuestion]) => ProcessAnswer(r, answer)) onComplete {
                    case Success(m: NextQuestion) => ref ! m
                    case Failure(_) => ref ! Finished
                }
                Behaviors.same
            case SessionEnded(id) =>
                running(next, mapping - id)
        }
    }
}

