package io.github.mellivorines.codegenerator

import com.intellij.database.psi.DbTable
import com.intellij.database.view.getSelectedPsiElements
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.ui.Messages
import io.github.mellivorines.codegenerator.model.setting.DataModel
import io.github.mellivorines.codegenerator.ui.DslConfigDialogUI
import org.jetbrains.jps.model.java.JavaSourceRootType

/**
 * main dialog
 *
 * @author mellivorines
 * @since 2023/5/13
 */
internal class GeneratorAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e)
        val allModules = project?.let { ModuleManager.getInstance(it) }?.sortedModules
        val psiElements = getSelectedPsiElements(e.dataContext)
        // Prompt that a table must be selected
        if (allModules.isNullOrEmpty() || psiElements.isEmpty) {
            Messages.showMessageDialog(project, "Select at least one table!", "Warning", Messages.getWarningIcon())
            return
        }

        val selectedTables = psiElements
            .filter { it is DbTable }
            .map { it as DbTable }
            .toList()
        // remove root modules
        val validModules = allModules
            .filter { ModuleRootManager.getInstance(it).getSourceRoots(JavaSourceRootType.SOURCE).isNotEmpty() }
            .toList()
        val dataModel = DataModel(validModules, selectedTables)
        // show dialog
        DslConfigDialogUI(project, dataModel, "CodeGenerator").show()
    }

    /**
     * only popup on the table
     */
    override fun update(e: AnActionEvent) {
        var visible = true
        val psiElements = getSelectedPsiElements(e.dataContext)
        if (psiElements.isEmpty || psiElements[0] !is DbTable) {
            visible = false
        }
        e.presentation.isEnabledAndVisible = visible
    }
}
