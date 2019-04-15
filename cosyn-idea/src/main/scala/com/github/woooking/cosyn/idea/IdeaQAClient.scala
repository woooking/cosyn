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
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid
import com.intellij.openapi.util.Computable
import com.intellij.psi.{JavaPsiFacade, PsiDocumentManager, PsiElement, PsiMethod}

import scala.collection.JavaConverters._

class IdeaQAClient(project: Project) extends ProjectComponent {
    private var server = QAServer.create

    private val logger = Logger.getInstance(this.getClass)

    def newTask(context: Context, psiMethod: PsiMethod, dataContext: DataContext): Unit = {
        logger.info(s"[New Task] ${context.query}")
        val (s, r) = server.startSession(context)
        server = s
        r match {
            case StartSessionResponseWithQuestion(sessionId, newContext, _, question) =>
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
        PsiDocumentManager.getInstance(psiMethod.getProject).performLaterWhenAllCommitted(() => {
            question match {
                case ChoiceQuestion(q, choices) =>
                    val actions = choices
                        .map {
                            case c: RecommendChoice => c.filled.toString
                            case c => c.toString
                        }
                        .zipWithIndex
                        .map {
                            case (c, i) => new AnAction(c) {
                                override def actionPerformed(e: AnActionEvent): Unit = {
                                    running(id, psiMethod, dataContext, s"#${i + 1}")
                                }
                            }
                        }
                    val actionGroup = new DefaultActionGroup(actions.asJava)
                    val popup = JBPopupFactory.getInstance()
                        .createActionGroupPopup(q, actionGroup, dataContext, ActionSelectionAid.SPEEDSEARCH, true, () => {}, 10)
                    popup.showInFocusCenter()
                case q @ ArrayInitQuestion(_, _) =>
                    val yesAction = new AnAction("Y") {
                        override def actionPerformed(e: AnActionEvent): Unit = {
                            running(id, psiMethod, dataContext, "Y")
                        }
                    }
                    val noAction = new AnAction("N") {
                        override def actionPerformed(e: AnActionEvent): Unit = {
                            running(id, psiMethod, dataContext, "N")
                        }
                    }
                    val actionGroup = new DefaultActionGroup(yesAction, noAction)
                    val popup = JBPopupFactory.getInstance()
                        .createActionGroupPopup(q.description, actionGroup, dataContext, ActionSelectionAid.SPEEDSEARCH, true, () => {}, 10)
                    popup.showInFocusCenter()
                case RecommendQuestion(wrapped, recommendations) =>
                    val recommendActions = recommendations.zipWithIndex.map { case (r, i) => new AnAction(r.filled.toString) {
                        override def actionPerformed(e: AnActionEvent): Unit = {
                            running(id, psiMethod, dataContext, s"#${i + 1}")
                        }
                    }
                    }

                    val inputAction = new AnAction(wrapped.description) {
                        override def actionPerformed(e: AnActionEvent): Unit = {
                            val dialog = new InputDialog(e.getProject, wrapped.description)
                            val result = dialog.showAndGet()
                            if (result) {
                                running(id, psiMethod, dataContext, dialog.getResult)
                            }
                        }
                    }
                    val actionGroup = new DefaultActionGroup(recommendActions :+ inputAction: _*)
                    val popup = JBPopupFactory.getInstance()
                        .createActionGroupPopup(wrapped.description, actionGroup, dataContext, ActionSelectionAid.SPEEDSEARCH, true, () => {}, 10)
                    popup.showInFocusCenter()
                case _ =>
                    val dialog = new InputDialog(psiMethod.getProject, question.description)
                    val result = dialog.showAndGet()
                    if (result) {
                        running(id, psiMethod, dataContext, dialog.getResult)
                    }
            }

        })

    }

    def running(id: Long, psiMethod: PsiMethod, dataContext: DataContext, answer: String): Unit = {
        logger.info(s"[User Input] $answer")
        val (s, r) = server.answer(id, answer)
        server = s
        r match {
            case QuestionFromSession(ctx, _, question) =>
                logger.info(s"[Server Response] Question from session: ${question.description}")
                waiting(id, psiMethod, dataContext, ctx, question)
            case Finished(ctx) =>
                logger.info(s"[Server Response] Finished")
                val code = s"{${ctx.pattern.stmts.generateCode("")}}"
                val factory = JavaPsiFacade.getInstance(project).getElementFactory
                val block = factory.createCodeBlockFromText(code, null)
                WriteCommandAction.runWriteCommandAction(psiMethod.getProject, new Computable[PsiElement]() {
                    override def compute(): PsiElement = psiMethod.getBody.replace(block)
                })
            case ErrorAnswer(ctx, _, question, message) =>
                logger.info(s"[Server Response] Error")
                Notifications.Bus.notify(new Notification("Cosyn", "Invalid Input", message, NotificationType.WARNING))
                waiting(id, psiMethod, dataContext, ctx, question)
        }
    }

    def start(psiMethod: PsiMethod, dataContext: DataContext, task: String, vars: Set[(String, Type)], extended: Seq[String]): Unit = {
        val context = Context(task, vars, null, extended, null)
        newTask(context, psiMethod, dataContext)
    }
}
