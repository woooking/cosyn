package com.github.woooking.cosyn.cmd

import com.github.woooking.cosyn.comm.skeleton.model.BasicType
import com.github.woooking.cosyn.comm.util.TimeUtil
import com.github.woooking.cosyn.core.code.{Context, Question}
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.qa._
import com.github.woooking.cosyn.kg.KnowledgeGraph
import org.slf4s.Logging

import scala.io.StdIn

object CmdQAClient extends Logging {
    final case class NewTask(context: Context) extends QAClientMessage

    def newTask(server: QAServer, context: Context): Unit = {
        val (s, r) = server.startSession(context)
        r match {
            case StartSessionResponseWithQuestion(sessionId, newContext, question) =>
                waiting(s, sessionId, newContext, question)
            case Finished(newContext) =>
                println(newContext.pattern.stmts)
            case ErrorOccurred(message) =>
                log.error(message)
        }
    }

    def running(server: QAServer, id: Long, answer: String): Unit = {
        val (s, r) = server.answer(id, answer)
        r match {
            case QuestionFromSession(ctx, question) =>
                waiting(s, id, ctx, question)
            case Finished(ctx) =>
                println("----- Code Generated -----")
                println(ctx.pattern.stmts)
                KnowledgeGraph.close()
                TimeUtil.print()
            case ErrorAnswer(ctx, question, message) =>
                println(message)
                waiting(server, id, ctx, question)
            case ErrorOccurred(message) =>
                println("----- Code Generated -----")
                println(message)
        }
    }

    def waiting(server: QAServer, id: Long, ctx: Context, question: Question): Unit = {
        if (Config.printCodeEachStep) {
            println("----- code -----")
            println(ctx.pattern.stmts.generateCode(""))
            println("----- end -----")
        }
        println(question.description)
        val answer = StdIn.readLine()
        running(server, id, answer)
    }

    def main(args: Array[String]): Unit = {
        val task = StdIn.readLine()
        val context = Context(task, Set("sheet" -> BasicType("org.apache.poi.ss.usermodel.Sheet")), null, Seq("java.lang.Object"), null)
        newTask(QAServer.create, context)
    }
}
