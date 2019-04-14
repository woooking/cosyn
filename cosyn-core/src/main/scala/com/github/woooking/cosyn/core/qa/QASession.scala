package com.github.woooking.cosyn.core.qa

import com.github.woooking.cosyn.comm.skeleton.model.HoleExpr
import com.github.woooking.cosyn.core.code.Question.{ErrorInput, Filled, NewQuestion}
import com.github.woooking.cosyn.core.code.{Context, Question}
import com.github.woooking.cosyn.core.nlp.{NLMatcher, NLP}

trait QASession {
    def start(context: Context): (QASession, StartResponse) = ???

    def processAnswer(input: String): (QASession, ProcessAnswerResponse) = ???

    def processUndo: (QASession, ProcessUndoResponse) = ???
}

object QASession {

    private def setup(data: QuestionData): (QASession, StartResponse with ProcessAnswerResponse) = data match {
        case QuestionData(ctx, history) =>
            QuestionGenerator.generate(ctx) match {
                case Some((hole, q)) =>
                    (Waiting(WaitingData(ctx, hole, q, history)), QuestionFromSession(ctx, hole, q))
                case None =>
                    (Stopped, Finished(ctx))
            }
    }

    case object Stopped extends QASession

    case class Initializing() extends QASession {
        override def start(context: Context): (QASession, StartResponse) = {
            val matcher = new NLMatcher(context.query, 5)
            val patterns = matcher.find()
            patterns.foreach { case (p, s) =>
                println(s"----- $s -----")
                println(p.stmts.generateCode(""))
                println(s"----------")
            }
            val pattern = patterns.head._1
            val newCtx = context.copy(pattern = pattern, nlp = NLP.parse(context.query))
            setup(QuestionData(newCtx, Nil))
        }
    }

    case class Waiting(data: WaitingData) extends QASession {
        private val WaitingData(ctx, hole, question, history) = data

        override def processAnswer(input: String): (QASession, ProcessAnswerResponse) = {
            question.processInput(ctx, hole, input) match {
                case Filled(newCtx) =>
                    setup(QuestionData(newCtx, (ctx, hole, question) :: history))
                case NewQuestion(q) =>
                    (Waiting(WaitingData(ctx, hole, q, (ctx, hole, question) :: history)), QuestionFromSession(ctx, hole, q))
                case ErrorInput(message) =>
                    (this, ErrorAnswer(ctx, hole, question, message))
            }
        }

        override def processUndo: (QASession, ProcessUndoResponse) = {
            history match {
                case (lastCtx, lastHole, lastQuestion) :: rest =>
                    (Waiting(WaitingData(lastCtx, lastHole, lastQuestion, rest)), QuestionFromSession(lastCtx, lastHole, lastQuestion))
                case Nil =>
                    (this, CannotUndo)
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

