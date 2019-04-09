package com.github.woooking.cosyn.idea

import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.intellij.openapi.editor.Caret
import com.intellij.psi.{JavaRecursiveElementVisitor, PsiClass, PsiMethod, PsiParameter}

class PsiVisitor(caret: Caret) extends JavaRecursiveElementVisitor {
    var vars = Set.empty[(String, Type)]
    var extended = Array.empty[String]
    var psiMethod: PsiMethod = _

    override def visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        if (method.getTextRange.contains(caret.getOffset)) {
            vars = method.getParameterList.getParameters.toSet.map((p: PsiParameter) => p.getName -> string2type(p.getType.getCanonicalText))
            psiMethod = method
        }
    }

    override def visitClass(aClass: PsiClass) {
        super.visitClass(aClass)
        if (aClass.getTextRange.contains(caret.getOffset)) {
            extended = aClass.getExtendsListTypes.map(_.getCanonicalText)
        }
    }
}
