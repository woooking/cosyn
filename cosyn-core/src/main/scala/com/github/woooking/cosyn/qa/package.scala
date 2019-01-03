package com.github.woooking.cosyn

import com.github.woooking.cosyn.code.Context

package qa {

    import akka.actor.typed.ActorRef
    import com.github.woooking.cosyn.code.{Pattern, Question}

    sealed trait StartSessionResponse

    final case class StartSessionResponseWithQuestion(sessionId: Long, context: Context, pattern: Pattern, question: Question) extends StartSessionResponse

    sealed trait QAServerMessage

    final case class StartSessionRequest(from: ActorRef[StartSessionResponse], context: Context, description: String) extends QAServerMessage

    final case class AnswerRequest(from: ActorRef[NextQuestion], sessionId: Long, answer: String) extends QAServerMessage

    final case class SessionEnded(sessionId: Long) extends QAServerMessage

    sealed trait NextQuestion extends QAServerMessage

    final case class QuestionFromSession(context: Context, pattern: Pattern, question: Question) extends NextQuestion

    case object Finished extends NextQuestion with StartSessionResponse

    sealed trait QASessionMessage

    final case class Start(from: ActorRef[QuestionFromSession], context: Context, description: String) extends QASessionMessage

    final case class ProcessAnswer(from: ActorRef[QuestionFromSession], input: String) extends QASessionMessage

}
