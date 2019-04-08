package com.github.woooking.cosyn.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class CosynAction extends AnAction {
    private static Logger logger = Logger.getInstance(CosynAction.class);
    public CosynAction() {
        super("Cosyn");
    }

    public void actionPerformed(AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        Caret caret = event.getData(LangDataKeys.CARET);
        if (psiFile != null && caret != null) {
            PsiElement psiElement = psiFile.findElementAt(caret.getOffset());
            if (psiElement instanceof PsiComment) {
                PsiComment psiComment = ((PsiComment) psiElement);
                String task = psiComment.getText();
                PsiVisitor visitor = new PsiVisitor(caret);
                psiFile.accept(visitor);
            }
        }
    }
}
