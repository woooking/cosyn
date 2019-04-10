package com.github.woooking.cosyn.core.qa

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.core.code.Question.{ErrorInput, Filled, NewQuestion}
import com.github.woooking.cosyn.core.code.{Context, Question}
import com.github.woooking.cosyn.core.nlp.{NLMatcher, NLP}

object QASession {

    val initializing: Behavior[QASessionMessage] = Behaviors.receiveMessagePartial { case Start(ref, ctx) =>
        val matcher = new NLMatcher(ctx.query, 5)
        val patterns = matcher.find()
        patterns.foreach { case (p, s) =>
            println(s"----- $s -----")
            println(p.stmts.generateCode(""))
            println(s"----------")
        }
        val pattern = patterns.head._1
        val newCtx = ctx.copy(pattern = pattern, nlp = NLP.parse(ctx.query))
        generate(ref, QuestionData(newCtx, Nil))
    }

    def generate(ref: ActorRef[StartResponse with ProcessAnswerResponse], data: QuestionData): Behavior[QASessionMessage] = data match {
        case QuestionData(ctx, history) => Behaviors.setup { _ =>
            QuestionGenerator.generate(ctx) match {
                case Some((hole, q)) =>
                    ref ! QuestionFromSession(ctx, q)
                    waiting(WaitingData(ctx, hole, q, history))
                case None =>
                    ref ! Finished(ctx)
                    Behaviors.stopped
            }
        }
    }

    def waiting(data: WaitingData): Behavior[QASessionMessage] = data match {
        case WaitingData(ctx, hole, question, history) =>
            Behaviors.receiveMessagePartial {
                case ProcessAnswer(ref, input) =>
                    question.processInput(ctx, hole, input) match {
                        case Filled(newCtx) =>
                            generate(ref, QuestionData(newCtx, (ctx, hole, question) :: history))
                        case NewQuestion(q) =>
                            ref ! QuestionFromSession(ctx, q)
                            waiting(WaitingData(ctx, hole, q, (ctx, hole, question) :: history))
                        case ErrorInput(message) =>
                            ref ! ErrorAnswer(ctx, question, message)
                            Behaviors.same
                    }
                case ProcessUndo(ref) =>
                    history match {
                        case (lastCtx, lastHole, lastQuestion) :: rest =>
                            ref ! QuestionFromSession(lastCtx, lastQuestion)
                            waiting(WaitingData(lastCtx, lastHole, lastQuestion, rest))
                        case Nil =>
                            ref ! CannotUndo
                            Behaviors.same
                    }
            }
    }

    case class QuestionData(context: Context,
                            history: List[(Context, HoleExpr, Question)])

    case class WaitingData(context: Context,
                           hole: HoleExpr,
                           question: Question,
                           history: List[(Context, HoleExpr, Question)])

}

