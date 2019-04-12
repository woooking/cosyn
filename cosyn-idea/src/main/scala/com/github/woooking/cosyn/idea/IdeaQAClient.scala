package com.github.woooking.cosyn.idea

import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.qa._
import com.github.woooking.cosyn.idea.ui.InputDialog
import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.actionSystem._
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import com.intellij.openapi.util.Computable
import com.intellij.psi.{JavaPsiFacade, PsiElement, PsiMethod}
import javax.swing._

import scala.collection.JavaConverters._

class IdeaQAClient(project: Project) extends ProjectComponent {
    private var server = QAServer.create

    private val logger = Logger.getInstance(this.getClass)

    def newTask(context: Context, psiMethod: PsiMethod, dataContext: DataContext): Unit = {
        logger.info(s"[New Task] ${context.query}")
        val (s, r) = server.startSession(context)
        server = s
        r match {
            case StartSessionResponseWithQuestion(sessionId, newContext, question) =>
                waiting(sessionId, psiMethod, dataContext, newContext, question)
            case Finished(newContext) =>
                println(newContext.pattern.stmts)
            case ErrorOccurred(message) =>
                logger.error(message)
        }
    }

    def waiting(id: Long, psiMethod: PsiMethod, dataContext: DataContext, ctx: Context, question: Question): Unit = {
        val code = s"{${ctx.pattern.stmts.generateCode("")}}"
        val factory = JavaPsiFacade.getInstance(project).getElementFactory
        val block = factory.createCodeBlockFromText(code, null)
        WriteCommandAction.runWriteCommandAction(psiMethod.getProject, new Computable[PsiElement]() {
            override def compute(): PsiElement = psiMethod.getBody.replace(block)
        })
        question match {
            case ChoiceQuestion(q, choices) =>
                val actions = choices.zipWithIndex.map { case (c, i) => new AnAction(c.toString) {
                    override def actionPerformed(e: AnActionEvent): Unit = {
                        running(id, psiMethod, dataContext, s"#${i + 1}")
                    }
                }
                }
                val actionGroup = new DefaultActionGroup(actions.asJava)
                val popup = JBPopupFactory.getInstance().createActionGroupPopup(q, actionGroup, dataContext, ActionSelectionAid.SPEEDSEARCH, true, () => {}, 10)
                popup.showInFocusCenter()
            case q @ ArrayInitQuestion(_, _) =>
                val popup = JBPopupFactory.getInstance().createConfirmation(
                    q.description,
                    "Yes",
                    "No",
                    () => running(id, psiMethod, dataContext, "Y"),
                    () => running(id, psiMethod, dataContext, "N"),
                    1
                )
                popup.showInFocusCenter()
            case RecommendQuestion(wrapped, recommendations) =>
                val recommendActions = recommendations.zipWithIndex.map { case (r, i) => new AnAction(r.toString) {
                    override def actionPerformed(e: AnActionEvent): Unit = {
                        running(id, psiMethod, dataContext, s"#${i + 1}")
                    }
                }
                }

                val inputAction = new AnAction() {
                    override def actionPerformed(e: AnActionEvent): Unit = {
                        val dialog = new InputDialog(e.getProject, wrapped.description)
                        val result = dialog.showAndGet()
                        if (result) {
                            running(id, psiMethod, dataContext, dialog.getResult)
                        }
                    }
                }
                val actionGroup = new DefaultActionGroup((recommendActions :+ inputAction).asJava)
                val popup = JBPopupFactory.getInstance().createActionGroupPopup(wrapped.description, actionGroup, dataContext, ActionSelectionAid.SPEEDSEARCH, true, () => {}, 10)
                popup.showInFocusCenter()
            case _ =>
                val dialog = new InputDialog(psiMethod.getProject, question.description)
                val result = dialog.showAndGet()
                if (result) {
                    running(id, psiMethod, dataContext, dialog.getResult)
                }
        }
    }

    def running(id: Long, psiMethod: PsiMethod, dataContext: DataContext, answer: String): Unit = {
        logger.info(s"[User Input] $answer")
        val (s, r) = server.answer(id, answer)
        server = s
        r match {
            case QuestionFromSession(ctx, question) =>
                waiting(id, psiMethod, dataContext, ctx, question)
            case Finished(ctx) =>
                val code = s"{${ctx.pattern.stmts.generateCode("")}}"
                val factory = JavaPsiFacade.getInstance(project).getElementFactory
                val block = factory.createCodeBlockFromText(code, null)
                WriteCommandAction.runWriteCommandAction(psiMethod.getProject, new Computable[PsiElement]() {
                    override def compute(): PsiElement = psiMethod.getBody.replace(block)
                })
            case ErrorAnswer(ctx, question, message) =>
                Notifications.Bus.notify(new Notification("Cosyn", "Invalid Input", message, NotificationType.WARNING))
                waiting(id, psiMethod, dataContext, ctx, question)
        }
    }


    def start(psiMethod: PsiMethod, dataContext: DataContext, task: String, vars: Set[(String, Type)], extended: Seq[String]): Unit = {
        val context = Context(task, vars, null, extended, null)
        newTask(context, psiMethod, dataContext)
    }
}
