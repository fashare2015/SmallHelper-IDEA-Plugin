package com.fashare.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Created by jinliangshan on 16/11/23.
 */
class GeneratorAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        // TODO: insert action logic here
        Messages.showMessageDialog("Hello World !", "Information", Messages.getInformationIcon())
    }
}
