package io.github.mellivorines.codegenerator.meta

import kotlin.reflect.KClass

/**
 * column info
 *
 * @author mellivorines
 * @date 2022-10-10
 */
data class Column(
    /**
     * 列名
     */
    var name: String = "",

    /**
     * 是否为主键
     */
    var primaryKey: Boolean = false,

    /**
     * 自增
     */
    var autoGenerated: Boolean = false,

    /**
     * 包含在唯一索引中
     */
    var uniqueKey: Boolean = false,

    /**
     * 字段类型的字符串原值
     */
    var typeName: String = "",

    /**
     * java属性名称
     */
    var propName: String = name,

    /**
     * jvm语言类型
     */
    var jvmType: KClass<*> = String::class,

    /**
     * jvm语言名称
     */
    var jvmTypeName: String = "",

    /**
     * 长度
     */
    var length: Int = 0,

    /**
     * 精度
     */
    var scale: Int = 0,

    /**
     * 是否可为空
     */
    var nullable: Boolean = false,

    /**
     * 默认值
     */
    val defaultValue: String?,

    /**
     * 注释
     */
    var comment: String?

)