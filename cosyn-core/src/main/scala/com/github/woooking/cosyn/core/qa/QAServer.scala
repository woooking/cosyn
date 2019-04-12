package com.github.woooking.cosyn.core.qa

import com.github.woooking.cosyn.core.code.Context

trait QAServer {
    def startSession(context: Context): (QAServer, StartSessionResponse) = ???

    def answer(sessionId: Long, answer: String): (QAServer, AnswerResponse) = ???

    def undo(sessionId: Long): (QAServer, UndoResponse) = ???
}

object QAServer {
    def create: QAServer = Running(0, Map())

    private case class Running(next: Long, mapping: Map[Long, QASession]) extends QAServer {
        override def startSession(context: Context): (QAServer, StartSessionResponse) = {
            val session = QASession.Initializing()
            val (s, r) = session.start(context)
            val response = r match {
                case QuestionFromSession(newContext, question) =>
                    StartSessionResponseWithQuestion(next, newContext, question)
                case m: Finished  =>
                    // TODO: 回收资源
                    m
                case m: ErrorOccurred =>
                    m
            }
            (Running(next + 1, mapping + (next -> s)), response)
        }

        override def answer(sessionId: Long, answer: String): (QAServer, AnswerResponse) = {
            val session = mapping(sessionId)
            val (s, r) = session.processAnswer(answer)
            (this.copy(mapping = mapping.updated(sessionId, s)), r)
        }

        override def undo(sessionId: Long): (QAServer, UndoResponse) = {
            val session = mapping(sessionId)
            val (s, r) = session.processUndo
            (this.copy(mapping = mapping.updated(sessionId, s)), r)
        }
    }
}

