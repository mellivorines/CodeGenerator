package io.github.mellivorines.codegenerator.ui

import com.intellij.database.psi.DbTable
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.ui.JBEmptyBorder
import io.github.mellivorines.codegenerator.CodeGenerator
import io.github.mellivorines.codegenerator.GeneratorAction
import io.github.mellivorines.codegenerator.model.TypeRegistration
import io.github.mellivorines.codegenerator.model.setting.DataModel
import io.github.mellivorines.codegenerator.model.setting.GeneratorOptions
import io.github.mellivorines.codegenerator.util.buildOptions
import io.github.mellivorines.codegenerator.util.buildTable
import io.github.mellivorines.codegenerator.util.camelCase
import java.awt.Dimension
import javax.swing.JComponent


/**
 * @Description:
 *
 * @author lilinxi
 * @version 1.0.0
 * @since 2023/5/15
 */
class DslConfigDialogUI(private val project: Project, private val dataModel: DataModel, dialogTitle: String) :
    DialogWrapper(project, null, true, IdeModalityType.MODELESS, false) {

    private val log = Logger.getInstance(GeneratorAction::class.java)
    private val typeService = TypeRegistration.newInstance()
    private val options = initLogs(buildOptions(dataModel.modules[0]), dataModel.tables)

    init {
        title = dialogTitle
        init()
    }

    override fun createCenterPanel(): JComponent {
        val tabPanel = JBTabbedPane()
        tabPanel.minimumSize = Dimension(400, 450)
        tabPanel.preferredSize = Dimension(800, 900)


        val genDialog = GeneratorDialog(project, options, dataModel)


        val genPanel = genDialog.initPanel()

        val genScrollPanel = panel {
            row {
                genPanel.border = JBEmptyBorder(10)
                scrollCell(genPanel)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .resizableColumn()
            }
                .resizableRow()
                .layout(RowLayout.INDEPENDENT)

            row {
                button("Cancel") {
                    super.close(0, true)
                }.horizontalAlign(HorizontalAlign.RIGHT)
                    .resizableColumn()

                button("Generate") {
                    genDialog.optionPanel.apply()
                    // validate
                    if (options.author.isEmpty()) {
                        Messages.showMessageDialog("Author Can't be empty!", "Warning", null)
                    } else if (options.rootPackage.isEmpty()) {
                        Messages.showMessageDialog("Package Can't be empty!", "Warning", null)
                    } else if (!options.entityType) {
                        Messages.showMessageDialog("FileType Can't be empty!", "Warning", null)
                    } else {
                        // fetch table data
                        val tableList = dataModel.tables.map {
                            buildTable(typeService, it, options.excludeCols, options.language)
                        }
                        try {
                            val outLogs = CodeGenerator(project, options, tableList).generate()
                            genDialog.logger.flush(outLogs)
                            notify("Generate code succeed!")
                        } catch (e: RuntimeException) {
                            log.error("Generate Error: ${e.message}")
                            notify("Generate code failed! ${e.message}", NotificationType.ERROR)
                        }
                    }
                }
            }.layout(RowLayout.PARENT_GRID)
                .topGap(TopGap.SMALL)
        }
        // generator dialog
        tabPanel.add("Generator", genScrollPanel)

        // type dialog
        val typesPanel = TypesDialog(project).initPanel()
        tabPanel.add("RegisteredType", typesPanel)

        return tabPanel
    }

    /**
     * Popup Notifications
     */
    private fun notify(content: String, notifyType: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("io.github.mellivorines.CodeGenerator.Notification")
            .createNotification(content, notifyType)
            .notify(project)
    }

    /**
     * build logs
     */
    private fun initLogs(genOpts: GeneratorOptions, tables: List<DbTable>): GeneratorOptions {
        val maxLen = tables.maxOf { it.name.length }
        val logs = tables.joinToString(separator = "\n") {
            "[${it.name.padEnd(maxLen)}]  ==>  [${camelCase(it.name, true)}]"
        }

        genOpts.logs += "============================Mapping [Table] And [Class]============================\n"
        genOpts.logs += logs
        return genOpts
    }


}