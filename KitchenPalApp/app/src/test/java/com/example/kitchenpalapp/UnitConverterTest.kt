package com.example.kitchenpalapp

import com.example.kitchenpalapp.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 单位换算工具单元测试。
 */
class UnitConverterTest {

    @Test
    fun normalize_mapsSynonyms() {
        assertEquals("大勺", UnitConverter.normalize("汤匙"))
        assertEquals("大勺", UnitConverter.normalize("勺"))
        assertEquals("小勺", UnitConverter.normalize("茶匙"))
        assertEquals("毫升", UnitConverter.normalize("ml"))
        assertEquals("克", UnitConverter.normalize("g"))
        assertNull(UnitConverter.normalize("  "))
    }

    @Test
    fun toMetric_volumeUnits() {
        assertEquals(30.0, UnitConverter.toMetric(2.0, "勺")!!.value, 0.001)
        assertEquals(10.0, UnitConverter.toMetric(2.0, "小勺")!!.value, 0.001)
        assertEquals(500.0, UnitConverter.toMetric(2.0, "碗")!!.value, 0.001)
    }

    @Test
    fun toMetric_weightUnits() {
        assertEquals(150.0, UnitConverter.toMetric(3.0, "两")!!.value, 0.001)
        assertEquals(1000.0, UnitConverter.toMetric(2.0, "斤")!!.value, 0.001)
        assertEquals(200.0, UnitConverter.toMetric(200.0, "克")!!.value, 0.001)
    }

    @Test
    fun toMetric_countUnitReturnsNull() {
        assertNull(UnitConverter.toMetric(2.0, "个"))
        assertNull(UnitConverter.toMetric(1.0, "把"))
        assertNull(UnitConverter.toMetric(0.0, "勺"))
        assertNull(UnitConverter.toMetric(null, "勺"))
    }

    @Test
    fun hintFromAmountText_parses() {
        assertEquals("约 30 毫升", UnitConverter.hintFromAmountText("2勺"))
        assertEquals("约 150 克", UnitConverter.hintFromAmountText("3 两"))
        assertEquals("约 250 克", UnitConverter.hintFromAmountText("0.5斤"))
        assertNull(UnitConverter.hintFromAmountText("2个"))
        assertNull(UnitConverter.hintFromAmountText("适量"))
    }

    @Test
    fun isConvertible_flags() {
        assertTrue(UnitConverter.isConvertible("勺"))
        assertTrue(UnitConverter.isConvertible("两"))
        assertFalse(UnitConverter.isConvertible("个"))
        assertFalse(UnitConverter.isConvertible(null))
    }
}
