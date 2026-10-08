package com.example.kitchenpalapp.util

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 烹饪常用单位换算工具。
 *
 * 后端菜谱的食材用量是以「中文家常单位」记录的（如「2 勺」「1 碗」「3 两」），
 * 用户在下厨时往往需要换算成更直观的克/毫升/个等。本工具做两类事：
 *  1. [normalize]：把五花八门的写法归一到标准单位名（勺/汤匙→大勺 等）。
 *  2. [toMetric]：把家常单位按经验系数折算成克/毫升，返回可读文案。
 *
 * 换算系数均为**家常估算值**（不同食材密度差异大），仅用于下厨参考，
 * 不适用于营养学精确计算——营养统计仍以后端 calories 折算为准。
 */
object UnitConverter {

    /** 标准体积单位（1 大勺 ≈ 15ml，1 小勺 ≈ 5ml，1 杯 ≈ 240ml）。 */
    private data class VolUnit(val name: String, val ml: Double)

    private val volumeUnits = listOf(
        VolUnit("大勺", 15.0), VolUnit("汤匙", 15.0), VolUnit("勺", 15.0),
        VolUnit("小勺", 5.0), VolUnit("茶匙", 5.0),
        VolUnit("杯", 240.0), VolUnit("碗", 250.0),
        VolUnit("毫升", 1.0), VolUnit("ml", 1.0)
    )

    /** 标准重量单位（1 斤 = 500g，1 两 = 50g）。 */
    private val weightUnits = listOf(
        "两" to 50.0, "斤" to 500.0, "克" to 1.0, "g" to 1.0, "千克" to 1000.0, "kg" to 1000.0
    )

    /** 计数单位（不做换算，原样保留）。 */
    private val countUnits = setOf("个", "只", "条", "根", "颗", "瓣", "片", "块", "把", "束", "朵", "张", "尾")

    /**
     * 归一化单位写法：把同义单位统一成展示用标准名。
     * 如「汤匙」「勺」→「大勺」，「茶匙」→「小勺」，「ml」→「毫升」。
     */
    fun normalize(unit: String?): String? {
        val u = unit?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return when (u) {
            "汤匙", "勺", "大匙" -> "大勺"
            "茶匙", "小匙" -> "小勺"
            "ml", "mL", "ML" -> "毫升"
            "g", "G" -> "克"
            "kg", "KG" -> "千克"
            "ml.", "克." -> u.trimEnd('.')
            else -> u
        }
    }

    /** 该单位是否可参与克/毫升折算。 */
    fun isConvertible(unit: String?): Boolean {
        val u = normalize(unit) ?: return false
        return volumeUnits.any { it.name == u } || weightUnits.any { it.first == u }
    }

    /**
     * 把「数量 + 单位」折算成克（重量类）或毫升（体积类）。
     * 不可换算（计数单位 / 未知单位 / 数量非法）时返回 null。
     */
    fun toMetric(amount: Double?, unit: String?): MetricResult? {
        if (amount == null || amount <= 0) return null
        val u = normalize(unit) ?: return null

        volumeUnits.firstOrNull { it.name == u }?.let {
            return MetricResult(amount * it.ml, "ml", "毫升")
        }
        weightUnits.firstOrNull { it.first == u }?.let {
            return MetricResult(amount * it.second, "g", "克")
        }
        return null
    }

    /**
     * 生成给用户看的换算文案，例如：
     *  「2 勺」→「约 30 毫升」
     *  「3 两」→「约 150 克」
     *  「2 个」→ null（不可换算，不显示）
     */
    fun hint(amount: Double?, unit: String?): String? {
        val m = toMetric(amount, unit) ?: return null
        return "约 ${fmt(m.value)} ${m.cnUnit}"
    }

    /**
     * 是否为「计数单位」——用于 UI 判断是否显示「约 xx 克/毫升」。
     */
    fun isCountUnit(unit: String?): Boolean {
        val u = normalize(unit) ?: return false
        return countUnits.contains(u) || !isConvertible(u)
    }

    private fun fmt(v: Double): String =
        if (abs(v - v.roundToInt()) < 0.05) v.roundToInt().toString()
        else String.format("%.1f", v)

    /**
     * 解析「数量 + 单位」合并字符串（如「2勺」「200g」「3 两」「2个」），
     * 返回换算文案；无法解析或不可换算时返回 null。
     */
    fun hintFromAmountText(amountText: String?): String? {
        val raw = amountText?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        // 数字（含小数）在前，单位在后；也可能单位在前（如「g 200」不常见，忽略）
        val m = Regex("^([0-9]+(?:\\.[0-9]+)?)\\s*(.*)$").find(raw) ?: return null
        val num = m.groupValues[1].toDoubleOrNull() ?: return null
        val unit = m.groupValues[2].trim().ifBlank { null }
        return hint(num, unit)
    }

    data class MetricResult(val value: Double, val unit: String, val cnUnit: String)
}
