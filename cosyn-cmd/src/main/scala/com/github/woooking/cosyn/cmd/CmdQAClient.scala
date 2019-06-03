package com.github.woooking.cosyn.cmd

import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, HoleExpr}
import com.github.woooking.cosyn.comm.util.TimeUtil
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.qa._
import com.github.woooking.cosyn.kg.KnowledgeGraph
import org.slf4s.Logging

import scala.io.StdIn

object CmdQAClient extends Logging {

    final case class NewTask(context: Context) extends QAClientMessage

    def newTask(server: QAServer, context: Context): Unit = {
        val (s, r) = server.startSession(context, 0)
        r match {
            case StartSessionResponseWithQuestion(sessionId, newContext, hole, question) =>
                waiting(s, sessionId, newContext, hole, question)
            case Finished(newContext) =>
                println(newContext.pattern.stmts)
            case ErrorOccurred(message) =>
                log.error(message)
        }
    }

    def running(server: QAServer, id: Long, answer: String): Unit = {
        val (s, r) = TimeUtil.profile("response") { server.answer(id, answer) }
        r match {
            case QuestionFromSession(ctx, hole, question) =>
                waiting(s, id, ctx, hole, question)
            case Finished(ctx) =>
                println("----- Code Generated -----")
                println(ctx.pattern.stmts)
                KnowledgeGraph.close()
                TimeUtil.print()
            case ErrorAnswer(ctx, hole, question, message) =>
                println(message)
                waiting(server, id, ctx, hole, question)
            case ErrorOccurred(message) =>
                println("----- Code Generated -----")
                println(message)
        }
    }

    def waiting(server: QAServer, id: Long, ctx: Context, hole: HoleExpr, question: Question): Unit = {
        if (Config.printCodeEachStep) {
            println("----- code -----")
            println(ctx.pattern.stmts.generateCode(""))
            println("----- end -----")
        }
        question match {
            case ChoiceQuestion(q, choices) =>
                val choiceString = choices
                    .map {
                        case c: RecommendChoice => c.filled.toString
                        case c => c.toString
                    }
                    .zipWithIndex
                    .map(p => s"#${p._2 + 1}. ${p._1}").mkString("\n")
                println(s"$q\n$choiceString")
            case RecommendQuestion(wrapped, recommendations) =>
                val choiceString = recommendations
                    .map(c => c.filled.toString)
                    .zipWithIndex
                    .map(p => s"#${p._2 + 1}. ${p._1}").mkString("\n")
                println(s"${wrapped.description}\n$choiceString")
            case _ =>
                println(question.description)
        }
        val answer = StdIn.readLine()
        running(server, id, answer)
    }

    def main(args: Array[String]): Unit = {
        val task = StdIn.readLine()
        val context = Context(task, Set("sheet" -> BasicType("org.apache.poi.ss.usermodel.Sheet")), null, Seq("java.lang.Object"), null)
        newTask(QAServer.create, context)
    }
}
