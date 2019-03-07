package com.github.woooking.cosyn.qa

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.BasicType
import com.github.woooking.cosyn.code.{Context, Question}
import com.github.woooking.cosyn.config.Config

import scala.io.StdIn

object CmdQAClient {
    val client: ActorSystem[QAClientMessage] = ActorSystem(idle, "cmd-client")
    val server: ActorSystem[QAServerMessage] = ActorSystem(QAServer.running(0, Map()), "qa-server")

    def idle: Behavior[QAClientMessage] = Behaviors.receivePartial {
        case (context, NewTask(ctx, description)) =>
            server ! StartSession(context.self, ctx, description)
            Behaviors.same
        case (_, StartSessionResponseWithQuestion(id, ctx, description, question)) =>
            waiting(id, ctx, description, question)
        case (_, Finished(_, pattern)) =>
            println(pattern.stmts)
            Behaviors.stopped
    }

    def running(id: Long, answer: String): Behavior[QAClientMessage] = Behaviors.setup { context =>
        server ! Answer(context.self, id, answer)
        Behaviors.receiveMessagePartial {
            case QuestionFromSession(ctx, pattern, question) =>
                waiting(id, ctx, pattern, question)
            case Finished(_, pattern) =>
                println("----- Code Generated -----")
                println(pattern.stmts)
                Behaviors.stopped
            case ErrorAnswer(ctx, pattern, question, message) =>
                println(message)
                waiting(id, ctx, pattern, question)
            case ErrorOccured(message) =>
                println("----- Code Generated -----")
                println(message)
                Behaviors.stopped
        }
    }

    def waiting(id: Long, ctx: Context, pattern: Pattern, question: Question): Behavior[QAClientMessage] = Behaviors.setup { _ =>
        if (Config.printCodeEachStep) {
            println("----- code -----")
            println(pattern.stmts.generateCode(""))
            println("----- end -----")
        }
        println(question.description)
        val answer = StdIn.readLine()
        running(id, answer)
    }

    def main(args: Array[String]): Unit = {
        // ---- case 1 ----
        val context = Context(Set("sheet" -> BasicType("org.apache.poi.ss.usermodel.Sheet")), Seq("java.lang.Object"))
        val task = StdIn.readLine()
        client ! NewTask(context, task)
    }
}
