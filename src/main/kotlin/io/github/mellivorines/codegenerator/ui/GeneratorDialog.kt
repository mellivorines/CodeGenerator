package io.github.mellivorines.codegenerator.ui

import com.intellij.database.view.actions.font
import com.intellij.ide.util.PackageChooserDialog
import com.intellij.ide.util.TreeJavaClassChooserDialog
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import io.github.mellivorines.codegenerator.model.setting.*
import io.github.mellivorines.codegenerator.util.buildOptions
import java.awt.BorderLayout
import java.awt.Font

/**
 * Generator Dialog ui
 *
 * @author mellivorines
 * @since 2023/5/13
 */
class GeneratorDialog(
    private val project: Project?,
    private val options: GeneratorOptions,
    private val data: DataModel
) {

    private lateinit var outDir: Cell<TextFieldWithBrowseButton>
    private lateinit var textPkg: Cell<JBTextField>
    private lateinit var txtLog: Cell<JBTextArea>
    private lateinit var chkEntity: Cell<JBCheckBox>
    private lateinit var chkInput: Cell<JBCheckBox>
    private lateinit var chkRepository: Cell<JBCheckBox>

    lateinit var logger: LoggerComponent
    lateinit var optionPanel: DialogPanel

    fun initPanel(): DialogPanel {
        val genPanel = DialogPanel(BorderLayout())
        optionPanel = initOptionPanel()
        genPanel.add(optionPanel, BorderLayout.NORTH)

        val logPanel = panel {
            group("Logs") {
                row {
                    txtLog = textArea().rows(10)
                        .horizontalAlign(HorizontalAlign.FILL)
                        .bindText(options::logs)
                        .font(Font("Hack", Font.ROMAN_BASELINE, 14))
                    txtLog.component.isEditable = false
                }
            }
        }

        logger = LoggerComponent(txtLog)
        val tablePanel = StrategyTableInfo(data.tables, options, logger).initTable()
        genPanel.add(tablePanel, BorderLayout.CENTER)

        // add log panel
        genPanel.add(logPanel, BorderLayout.SOUTH)

        return genPanel
    }

    private fun initOptionPanel(): DialogPanel {
        return panel {
            row("Module:") {
                val cmbModule = comboBox(data.modules)
                    .bindItem(options::activeModule.toNullableProperty())
                    .comment("Select the module")
                // add item changed event
                cmbModule.component.addActionListener {
                    val selectedIndex = (it.source as ComboBox<*>).selectedIndex
                    val curOpts = buildOptions(data.modules[selectedIndex])
                    outDir.text(curOpts.outputDir)
                    textPkg.text(curOpts.rootPackage)
                }

                textPkg = textField().label("Package: ")
                    .bindText(options::rootPackage)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .resizableColumn()
                    .comment("Select root package")
                button("Choose") {
                    val chooserDialog = PackageChooserDialog("Choose Package", project)
                    chooserDialog.show()
                    val psiPackage = chooserDialog.selectedPackage
                    if (psiPackage != null) {
                        textPkg.text(psiPackage.qualifiedName)
                    }
                }
            }.layout(RowLayout.PARENT_GRID)

            row("Author: ") {
                textField()
                    .bindText(options::author)
                    .comment("Input your name")
                val superCls = textField()
                    .label("SuperClass: ")
                    .bindText(options::superClass)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .resizableColumn()
                    .comment("Select the superclass of entity. e.g.: com.hello.entity.BaseEntity")
                button("Choose") {
                    val classChooserDialog = TreeJavaClassChooserDialog("Choose SuperClass", project)
                    classChooserDialog.show()
                    val selected = classChooserDialog.selected
                    if (selected != null) {
                        selected.qualifiedName?.let { pkg -> superCls.text(pkg) }
                    }
                }
            }.layout(RowLayout.PARENT_GRID)

            row("OutputDir: ") {
                outDir = textFieldWithBrowseButton(
                    project = project,
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                ).bindText(options::outputDir)
                    .gap(RightGap.SMALL)
                    .horizontalAlign(HorizontalAlign.FILL)
            }.layout(RowLayout.PARENT_GRID)
                .rowComment("Select the output directory. e.g.: /Project/src/main/java")

            twoColumnsRow({
                panel {
                    buttonsGroup {
                        row("FileMode: ") {
                            FileMode.values().forEach {
                                radioButton(it.name, it)
                            }
                        }.rowComment("Select the file output method")
                    }.bind(options::fileMode)

                    row("FileType: ") {
                        chkEntity = checkBox(FileType.Entity.name).bindSelected(options::entityType)
                        chkInput = checkBox(FileType.Input.name)
                            .bindSelected(options::inputType)
                            .enabledIf(chkEntity.selected)
                        chkRepository = checkBox(FileType.Repository.name)
                            .bindSelected(options::repositoryType)
                            .enabledIf(chkEntity.selected)
                    }.rowComment("Entity type must be selected!")
                }
            }, {
                panel {
                    buttonsGroup {
                        row("Language: ") {
                            Language.values().forEach {
                                radioButton(it.name, it)
                            }
                        }.rowComment("Select the language")
                    }.bind(options::language)

                    buttonsGroup {
                        row("Framework: ") {
                            Framework.values().forEach {
                                radioButton(it.name, it)
                            }
                        }.rowComment("Select the framework")
                    }.bind(options::framework)
                }
            }).layout(RowLayout.INDEPENDENT)

            row {
                textField().label("Exclude columns: ")
                    .bindText(options::excludeCols)
                    .horizontalAlign(HorizontalAlign.FILL)
            }.rowComment("Use commas to separate multiple items")
            row {
                label("Configure naming rules")
                contextHelp(
                    "1.The naming rule object is the [original table name]  " +
                            "2.The test function is only for [table objects]  " +
                            "3.Test the configuration using the [last button] on the toolbar.",
                    "Usage help"
                )
            }
        }
    }
}



