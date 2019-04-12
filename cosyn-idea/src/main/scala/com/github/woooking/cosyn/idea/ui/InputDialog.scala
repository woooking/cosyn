package com.github.woooking.cosyn.idea.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing._

class InputDialog(project: Project, question: String) extends DialogWrapper(true) {
    init()
    setTitle(question)

    val panel = new JPanel
    val input = new JTextField()
    panel.add(input)

    override def createCenterPanel(): JComponent = panel

    def getResult: String = input.getText()

}
