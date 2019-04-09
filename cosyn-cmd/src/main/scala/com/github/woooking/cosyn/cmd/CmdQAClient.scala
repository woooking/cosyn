package com.github.woooking.cosyn.cmd

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.github.woooking.cosyn.comm.skeleton.model.BasicType
import com.github.woooking.cosyn.comm.util.TimeUtil
import com.github.woooking.cosyn.core.code.{Context, Question}
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.core.qa._
import com.github.woooking.cosyn.kg.KnowledgeGraph

import scala.io.StdIn

object CmdQAClient {
    final case class NewTask(context: Context) extends QAClientMessage

    val client: ActorSystem[QAClientMessage] = ActorSystem(idle, "cmd-client")
    val server: ActorSystem[QAServerMessage] = ActorSystem(QAServer.running(0, Map()), "qa-server")

    def idle: Behavior[QAClientMessage] = Behaviors.receivePartial {
        case (context, NewTask(ctx)) =>
            server ! StartSession(context.self, ctx)
            Behaviors.same
        case (_, StartSessionResponseWithQuestion(id, ctx, question)) =>
            waiting(id, ctx, question)
        case (_, Finished(ctx)) =>
            println(ctx.pattern.stmts)
            Behaviors.stopped
    }

    def running(id: Long, answer: String): Behavior[QAClientMessage] = Behaviors.setup { context =>
        server ! Answer(context.self, id, answer)
        Behaviors.receiveMessagePartial {
            case QuestionFromSession(ctx, question) =>
                waiting(id, ctx, question)
            case Finished(ctx) =>
                println("----- Code Generated -----")
                println(ctx.pattern.stmts)
                KnowledgeGraph.close()
                TimeUtil.print()
                Behaviors.stopped
            case ErrorAnswer(ctx, question, message) =>
                println(message)
                waiting(id, ctx, question)
            case ErrorOccured(message) =>
                println("----- Code Generated -----")
                println(message)
                Behaviors.stopped
        }
    }

    def waiting(id: Long, ctx: Context, question: Question): Behavior[QAClientMessage] = Behaviors.setup { _ =>
        if (Config.printCodeEachStep) {
            println("----- code -----")
            println(ctx.pattern.stmts.generateCode(""))
            println("----- end -----")
        }
        println(question.description)
        val answer = StdIn.readLine()
        running(id, answer)
    }

    def main(args: Array[String]): Unit = {
        val task = StdIn.readLine()
        val context = Context(task, Set("sheet" -> BasicType("org.apache.poi.ss.usermodel.Sheet")), null, Seq("java.lang.Object"), null)
        client ! NewTask(context)
    }
}
