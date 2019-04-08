package com.github.woooking.cosyn.idea;

import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.intellij.openapi.editor.Caret
import com.intellij.psi.{JavaRecursiveElementVisitor, PsiMethod}

class PsiVisitor(caret: Caret) extends JavaRecursiveElementVisitor {
    var vars = Set.empty[(String, Type)]

    override def visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        if (method.getTextRange.contains(caret.getOffset)) {
            vars = method.getParameterList.getParameters.toSet.map(p => p.getName -> string2type(p.getType.getCanonicalText))
        }
    }
}
