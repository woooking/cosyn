package com.github.woooking.cosyn.idea.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing._

class InputDialog(project: Project, question: String) extends DialogWrapper(true) {
    setTitle(question)

    val input = new JTextField()

    init()

    override def createCenterPanel(): JComponent = input

    def getResult: String = input.getText()

}
