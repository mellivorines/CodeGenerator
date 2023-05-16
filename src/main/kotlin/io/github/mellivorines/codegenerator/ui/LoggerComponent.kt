package io.github.mellivorines.codegenerator.ui

import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.Cell
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * @Description:
 *
 * @author lilinxi
 * @version 1.0.0
 * @since 2023/5/14
 */
class LoggerComponent(private val txtLog: Cell<JBTextArea>) {
    /**
     * 输出日志
     */
    fun flush(lines: String) {
        val logComp = txtLog.component
        val nowTime = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .format(LocalDateTime.now())
        val timeLine = ">>>>>>>>>>$nowTime"
        logComp.insert("\n$timeLine\n$lines\n", logComp.text.length)
        logComp.autoscrolls = true
    }
}