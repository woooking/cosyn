package com.github.woooking.cosyn.qa

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.github.woooking.cosyn.code.Question.{ErrorInput, Filled, NewQuestion}
import com.github.woooking.cosyn.skeleton.model.HoleExpr
import com.github.woooking.cosyn.code.{Context, Question}
import com.github.woooking.cosyn.knowledge_graph.NLMatcher
import com.github.woooking.cosyn.skeleton.Pattern

object QASession {

    val initializing: Behavior[QASessionMessage] = Behaviors.receiveMessagePartial { case Start(ref, ctx, description) =>
        val matcher = new NLMatcher(description, 5)
        val patterns = matcher.find()
        patterns.foreach { case (p, s) =>
            println(s"----- $s -----")
            println(p.stmts.generateCode(""))
            println(s"----------")
        }
        val pattern = patterns.head._1
        val newCtx = ctx.update(pattern)
        generate(ref, QuestionData(newCtx, pattern, Nil))
    }

    def generate(ref: ActorRef[StartResponse with ProcessAnswerResponse], data: QuestionData): Behavior[QASessionMessage] = data match {
        case QuestionData(ctx, pattern, history) => Behaviors.setup { _ =>
            QuestionGenerator.generate(ctx, pattern) match {
                case Some((hole, q)) =>
                    ref ! QuestionFromSession(ctx, pattern, q)
                    waiting(WaitingData(ctx, pattern, hole, q, history))
                case None =>
                    ref ! Finished(ctx, pattern)
                    Behaviors.stopped
            }
        }
    }

    def waiting(data: WaitingData): Behavior[QASessionMessage] = data match {
        case WaitingData(ctx, pattern, hole, question, history) =>
            Behaviors.receiveMessagePartial {
                case ProcessAnswer(ref, input) =>
                    question.processInput(ctx, pattern, hole, input) match {
                        case Filled(newCtx, newPattern) =>
                            generate(ref, QuestionData(newCtx, newPattern, (ctx, pattern, hole, question) :: history))
                        case NewQuestion(q) =>
                            ref ! QuestionFromSession(ctx, pattern, q)
                            waiting(WaitingData(ctx, pattern, hole, q, (ctx, pattern, hole, question) :: history))
                        case ErrorInput(message) =>
                            ref ! ErrorAnswer(ctx, pattern, question, message)
                            Behaviors.same
                    }
                case ProcessUndo(ref) =>
                    history match {
                        case (lastCtx, lastPattern, lastHole, lastQuestion) :: rest =>
                            ref ! QuestionFromSession(lastCtx, lastPattern, lastQuestion)
                            waiting(WaitingData(lastCtx, lastPattern, lastHole, lastQuestion, rest))
                        case Nil =>
                            ref ! CannotUndo
                            Behaviors.same
                    }
            }
    }

    case class QuestionData(context: Context,
                            pattern: Pattern,
                            history: List[(Context, Pattern, HoleExpr, Question)])

    case class WaitingData(context: Context,
                           pattern: Pattern,
                           hole: HoleExpr,
                           question: Question,
                           history: List[(Context, Pattern, HoleExpr, Question)])

}

