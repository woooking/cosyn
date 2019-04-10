package com.github.woooking.cosyn.idea

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.qa._
import com.github.woooking.cosyn.idea.IdeaQAClient.{NewTask, UserAnswer}
import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.ui.popup.{JBPopupFactory, JBPopupListener, LightweightWindowEvent}
import com.intellij.psi.{JavaPsiFacade, PsiMethod}
import javax.swing.{BoxLayout, JLabel, JPanel, JTextField}

class IdeaQAClient(project: Project) extends ProjectComponent {

    val client: ActorSystem[QAClientMessage] = ActorSystem(idle, "idea-client")
    val server: ActorSystem[QAServerMessage] = ActorSystem(QAServer.running(0, Map()), "qa-server")

    def idle: Behavior[QAClientMessage] = Behaviors.receivePartial {
        case (context, NewTask(ctx, psiMethod)) =>
            server ! StartSession(context.self, ctx)
            initWaiting(psiMethod)
    }

    def initWaiting(psiMethod: PsiMethod): Behavior[QAClientMessage] = Behaviors.receivePartial {
        case (_, StartSessionResponseWithQuestion(id, ctx, question)) =>
            waiting(id, psiMethod, ctx, question)
        case (_, Finished(ctx)) =>
            // TODO: end
            println(ctx.pattern.stmts)
            Behaviors.stopped
    }

    def running(id: Long, psiMethod: PsiMethod, answer: String): Behavior[QAClientMessage] = Behaviors.setup { context =>
        server ! Answer(context.self, id, answer)
        Behaviors.receiveMessagePartial {
            case QuestionFromSession(ctx, question) =>
                waiting(id, psiMethod, ctx, question)
            case Finished(ctx) =>
                val code = s"{${ctx.pattern.stmts.generateCode("")}}"
                val factory = JavaPsiFacade.getInstance(project).getElementFactory
                val block = factory.createCodeBlockFromText(code, null)
                psiMethod.getBody.replace(block)
                idle
            case ErrorAnswer(ctx, question, message) =>
                Notifications.Bus.notify(new Notification("Cosyn", "Invalid Input", message, NotificationType.WARNING))
                waiting(id, psiMethod, ctx, question)
            case ErrorOccured(message) =>
                Notifications.Bus.notify(new Notification("Cosyn", "Error Occurred", message, NotificationType.ERROR))
                idle
        }
    }

    def waiting(id: Long, psiMethod: PsiMethod, ctx: Context, question: Question): Behavior[QAClientMessage] = Behaviors.setup { _ =>
        val code = s"{${ctx.pattern.stmts.generateCode("")}}"
        val factory = JavaPsiFacade.getInstance(project).getElementFactory
        val block = factory.createCodeBlockFromText(code, null)
        psiMethod.getBody.replace(block)
        question match {
            case ChoiceQuestion(q, choices) =>
                val listPopupStep = new BaseListPopupStep[String](q, choices.map(_.toString): _*)
                val popup = JBPopupFactory.getInstance().createListPopup(listPopupStep)
                popup.addListSelectionListener(e => client ! UserAnswer(s"#${e.getFirstIndex + 1}"))
                popup.showInFocusCenter()
            case q @ ArrayInitQuestion(_, _) =>
                val popup = JBPopupFactory.getInstance().createConfirmation(
                    q.description,
                    "Yes",
                    "No",
                    () => client ! UserAnswer("Y"),
                    () => client ! UserAnswer("N"),
                    1
                )
                popup.showInFocusCenter()
            case q @ (_: EnumConstantQuestion | _: StaticFieldAccessQuestion | _: PrimitiveQuestion) =>
                val panel = new JPanel
                val title = new JLabel(q.description)
                val input = new JTextField()
                panel.add(title)
                panel.add(input)
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
                val popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null)
                popup.addListener(new JBPopupListener {
                    override def onClosed(event: LightweightWindowEvent): Unit = {
                        client ! UserAnswer(input.getText)
                    }
                })
        }
        Behaviors.receiveMessagePartial {
            case UserAnswer(answer) =>
                running(id, psiMethod, answer)
            case _ =>
                Behavior.same
        }
    }

    def start(psiMethod: PsiMethod, task: String, vars: Set[(String, Type)], extended: Seq[String]): Unit = {
        val context = Context(task, vars, null, extended, null)
        client ! NewTask(context, psiMethod)
    }
}

object IdeaQAClient {
    final case class NewTask(context: Context, psiMethod: PsiMethod) extends QAClientMessage

    final case class UserAnswer(answer: String) extends QAClientMessage

}