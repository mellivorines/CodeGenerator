package io.github.mellivorines.codegenerator.ui

import com.intellij.database.psi.DbTable
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ListTableModel
import io.github.mellivorines.codegenerator.model.setting.*
import io.github.mellivorines.codegenerator.util.isNamingNormal
import io.github.mellivorines.codegenerator.util.naming
import java.awt.event.ItemEvent
import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.ListSelectionModel


/**
 * @Description:strategy table
 *
 * @author lilinxi
 * @version 1.0.0
 * @since 2023/5/15
 */
class StrategyTableInfo(
    private val tableList: List<DbTable>,
    private val options: GeneratorOptions,
    private val logger: LoggerComponent
) {
    private val tableModel = ListTableModel<StrategyRule>(
        StrategyTableColumnInfo("Operator"),
        StrategyTableColumnInfo("Target"),
        StrategyTableColumnInfo("Position"),
        StrategyTableColumnInfo("Value")
    )
    private var table = JBTable(tableModel)
    private val optItems = mapOf(
        0 to Operator.values().map { it.name },
        1 to OptTarget.values().map { it.name },
        2 to OptPosition.values().map { it.name }
    )

    init {
        table.apply {
            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
            tableHeader.reorderingAllowed = false
            rowSelectionAllowed = true
            fillsViewportHeight = true
            rowHeight = 25
        }
        // render columns with comboBox or text
        renderColumns()
    }

    /**
     * init strategy config table
     */
    fun initTable(): JBScrollPane {
        // add toolbar
        val decorator = ToolbarDecorator.createDecorator(table)
        // add "test" tool button
        val testButtonAction = TestButtonAction(tableList, tableModel, logger)
        // Disable when there is no data in the table
        testButtonAction.addCustomUpdater { tableModel.items.size > 0 }
        decorator.addExtraAction(testButtonAction)
        // override "add" event
        decorator.setAddAction { _ -> addDefaultRow() }
        // wrapper table with scroll
        return JBScrollPane(decorator.createPanel())
    }

    /**
     * render columns with comboBox or text
     */
    private fun renderColumns() {
        optItems.map { entry ->
            val comboBox = ComboBox(entry.value.toTypedArray())
            val column = table.columnModel.getColumn(entry.key)
            column.cellEditor = DefaultCellEditor(comboBox)

            // Add comboBox changed listener
            comboBox.addItemListener {
                if (it.stateChange == ItemEvent.SELECTED) {
                    val item = tableModel.getItem(table.selectedRow)
                    val itemValue = it.item as String
                    when (entry.key) {
                        0 -> item.operator = itemValue
                        1 -> item.target = itemValue
                        2 -> item.position = itemValue
                    }
                }
            }
        }

        val textField = JTextField("")
        val colText = table.columnModel.getColumn(3)
        colText.cellEditor = DefaultCellEditor(textField)
        textField.addActionListener { evt ->
            val item = tableModel.getItem(table.selectedRow)
            val optVal = evt.actionCommand?.trim() ?: ""
            if (optVal.isNotBlank() && isNamingNormal(optVal)) {
                item.optValue = optVal
            } else {
                logger.flush("Wrong naming [$optVal] !!")
            }
        }
    }

    /**
     * sync options strategy rules
     */
    private fun addDefaultRow() {
        tableModel.addRow(StrategyRule())
        options.strategyRules = tableModel.items
    }
}

/**
 * @author mellivorines
 * @date 2023-1-3
 */
private class TestButtonAction(
    private val dbTables: List<DbTable>,
    private val tableModel: ListTableModel<StrategyRule>,
    private val logger: LoggerComponent
) :
    AnActionButton("Test Rule...", AllIcons.Actions.Checked) {
    private lateinit var nameMapping: Map<String, String>

    override fun actionPerformed(e: AnActionEvent) {
        // Filter not blank value and table rules
        val filterRules = tableModel.items.filter { it.optValue.isNotBlank() && it.target == OptTarget.Table.name }
        if (filterRules.isEmpty()) {
            logger.flush("No applicable rules found! Please add rules first!")
            return
        }

        // Circular apply rules
        nameMapping = dbTables.map { tab ->
            mapOf(tab.name to naming(tab.name, filterRules))
        }.reduce { acc, map -> acc.plus(map) }

        val maxLen = dbTables.maxOf { it.name.length }
        logger.flush("\n========Test Mapping [Table] And [Class]========")
        val logs = nameMapping
            .map { (tab, cls) -> "[${tab.padEnd(maxLen)}]  ==>  [$cls]" }
            .joinToString(separator = "\n")
        logger.flush(logs)
    }
}