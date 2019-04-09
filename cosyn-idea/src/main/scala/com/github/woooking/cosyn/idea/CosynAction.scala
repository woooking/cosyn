package com.github.woooking.cosyn.idea

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, CommonDataKeys}
import com.intellij.psi.PsiComment

class CosynAction extends AnAction("Cosyn") {
    override def actionPerformed(e: AnActionEvent): Unit = {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val caret = e.getData(CommonDataKeys.CARET)
        if (psiFile != null && caret != null) {
            val psiElement = psiFile.findElementAt(caret.getOffset)
            psiElement match {
                case psiComment: PsiComment =>
                    val task = psiComment.getText
                    val visitor = new PsiVisitor(caret)
                    psiFile.accept(visitor)
                    val client = e.getProject.getComponent(classOf[IdeaQAClient])
                    client.start(visitor.psiMethod, task, visitor.vars, visitor.extended)
                case _ =>
            }
        }
    }
}
