package com.github.woooking.cosyn.core.qa {

    import akka.actor.typed.ActorRef
    import com.github.woooking.cosyn.comm.skeleton.Pattern
    import com.github.woooking.cosyn.core.code.{Context, Question}

    // QA Client 接受消息

    trait QAClientMessage

    sealed trait StartSessionResponse extends QAClientMessage

    final case class StartSessionResponseWithQuestion(sessionId: Long, context: Context, question: Question) extends StartSessionResponse

    sealed trait AnswerResponse extends QAClientMessage

    sealed trait UndoResponse extends QAClientMessage

    // QA Server 接受消息

    sealed trait QAServerMessage

    final case class ReplyToClient[T <: QAClientMessage](replyTo: ActorRef[T], message: T) extends QAServerMessage

    // QA Client -> QA Server

    final case class StartSession(from: ActorRef[StartSessionResponse], context: Context) extends QAServerMessage

    final case class Answer(from: ActorRef[QAClientMessage], sessionId: Long, answer: String) extends QAServerMessage

    final case class Undo(from: ActorRef[QAClientMessage], sessionId: Long) extends QAServerMessage

    // QA Session -> QA Server

    sealed trait StartResponse

    sealed trait ProcessAnswerResponse

    sealed trait ProcessUndoResponse

    final case class SessionEnded(sessionId: Long) extends QAServerMessage

    // QA Session 接受消息

    sealed trait QASessionMessage

    final case class Start(from: ActorRef[StartResponse], context: Context) extends QASessionMessage

    final case class ProcessAnswer(from: ActorRef[ProcessAnswerResponse], input: String) extends QASessionMessage

    final case class ProcessUndo(from: ActorRef[ProcessUndoResponse]) extends QASessionMessage

    //

    final case class QuestionFromSession(context: Context, question: Question) extends StartResponse
        with ProcessAnswerResponse with ProcessUndoResponse with AnswerResponse with UndoResponse

    final case class ErrorAnswer(context: Context, question: Question, message: String) extends ProcessAnswerResponse with AnswerResponse

    final case class Finished(context: Context) extends StartResponse with StartSessionResponse with ProcessAnswerResponse with AnswerResponse

    case object CannotUndo extends ProcessUndoResponse with UndoResponse

    final case class ErrorOccured(message: String) extends StartSessionResponse with AnswerResponse with UndoResponse with StartResponse with ProcessAnswerResponse

}
