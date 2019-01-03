package com.github.woooking.cosyn.qa

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.github.woooking.cosyn.code.model.HoleExpr
import com.github.woooking.cosyn.code.{Context, Pattern, Question}

object QASession {

    val initializing: Behavior[QASessionMessage] = Behaviors.receiveMessagePartial { case Start(ref, ctx, description) =>
        val pattern = Pattern.patterns(description)
        val newCtx = ctx.update(pattern)
        question(ref, SessionData(newCtx, pattern, Nil))
    }

    def question(ref: ActorRef[QuestionFromSession], data: SessionData): Behavior[QASessionMessage] = data match {
        case SessionData(ctx, pattern, history) => Behaviors.setup { _ =>
            QuestionGenerator.generate(ctx, pattern) match {
                case Some((hole, q)) =>
                    ref ! QuestionFromSession(ctx, pattern, q)
                    waiting(SessionData(ctx, pattern, (hole, q, ctx, pattern) :: history))
                case None =>
                    Behaviors.stopped
            }
        }
    }

    def waiting(data: SessionData): Behavior[QASessionMessage] = {
        val history = data.history
        history.head match {
            case (hole, question, ctx, pattern) => Behaviors.receiveMessagePartial { case ProcessAnswer(ref, input) =>
                val behavior: Behavior[QASessionMessage] = question.processInput(ctx, pattern, hole, input) match {
                    case Right((newCtx, newPattern)) =>
                        question(ref, SessionData(newCtx, newPattern, history))
                    case Left(q) =>
                        ref ! QuestionFromSession(ctx, pattern, q)
                        waiting(SessionData(ctx, pattern, (hole, q, ctx, pattern) :: history))
                }
                behavior
            }
        }
    }

    case class SessionData(context: Context,
                           pattern: Pattern,
                           history: List[(HoleExpr, Question, Context, Pattern)])

}

