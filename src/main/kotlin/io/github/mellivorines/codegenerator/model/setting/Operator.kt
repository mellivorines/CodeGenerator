package io.github.mellivorines.codegenerator.model.setting

import org.jetbrains.annotations.ApiStatus


/**
 * @Description:For StrategyDialog
 *
 * @author lilinxi
 * @version 1.0.0
 * @since 2023/5/13
 */

/**
 * 操作
 */
@ApiStatus.Internal
enum class Operator {
    Add, Remove;
}

/**
 * 操作目标
 */
@ApiStatus.Internal
enum class OptTarget {
    Table, Column
}

/**
 * 操作位置
 */
@ApiStatus.Internal
enum class OptPosition {
    Prefix, Suffix
}


// ==================For TypesDialog==================

enum class Tag {
    INTERNAL, CUSTOM
}