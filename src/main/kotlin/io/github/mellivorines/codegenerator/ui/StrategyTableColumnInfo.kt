package io.github.mellivorines.codegenerator.ui

import com.intellij.util.ui.ColumnInfo
import io.github.mellivorines.codegenerator.model.setting.StrategyRule


/**
 * @Description:render table columns
 *
 * @author lilinxi
 * @version 1.0.0
 * @since 2023/5/15
 */
class StrategyTableColumnInfo(name: String) : ColumnInfo<StrategyRule, String>(name) {

    override fun valueOf(item: StrategyRule): String {
        return when {
            name.equals("Operator") -> item.operator
            name.equals("Target") -> item.target
            name.equals("Position") -> item.position
            name.equals("Value") -> item.optValue
            else -> ""
        }
    }

    override fun isCellEditable(item: StrategyRule?): Boolean {
        return true
    }

    override fun setValue(item: StrategyRule?, value: String?) {
        super.setValue(item, value)
    }
}