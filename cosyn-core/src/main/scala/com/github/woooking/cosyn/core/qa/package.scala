package com.github.woooking.cosyn.core.qa {

    import com.github.woooking.cosyn.core.code.{Context, Question}

    // QA Client 接受消息

    trait QAClientMessage

    sealed trait StartSessionResponse extends QAClientMessage

    final case class StartSessionResponseWithQuestion(sessionId: Long, context: Context, question: Question) extends StartSessionResponse

    sealed trait AnswerResponse extends QAClientMessage

    sealed trait UndoResponse extends QAClientMessage

    // QA Server 接受消息

    sealed trait QAServerMessage

    // QA Session -> QA Server

    sealed trait StartResponse

    sealed trait ProcessAnswerResponse extends AnswerResponse

    sealed trait ProcessUndoResponse extends UndoResponse

    //

    final case class QuestionFromSession(context: Context, question: Question) extends StartResponse
        with ProcessAnswerResponse with ProcessUndoResponse

    final case class ErrorAnswer(context: Context, question: Question, message: String) extends ProcessAnswerResponse

    final case class Finished(context: Context) extends StartResponse with StartSessionResponse with ProcessAnswerResponse

    case object CannotUndo extends ProcessUndoResponse

    final case class ErrorOccurred(message: String) extends StartSessionResponse with UndoResponse with StartResponse with ProcessAnswerResponse

}
