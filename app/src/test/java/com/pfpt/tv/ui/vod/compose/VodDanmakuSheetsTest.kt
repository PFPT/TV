package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VodDanmakuSheetsTest {

    @Test
    fun blankKeywordDoesNotSearch() {
        assertFalse(danmakuSearchReady("   "))
    }

    @Test
    fun keywordSearchesAfterTrim() {
        assertTrue(danmakuSearchReady("  名称  "))
    }

    @Test
    fun styleRowsFollowMode() {
        assertEquals(DanmakuStyleRows(shadow = false, stroke = false, projection = false), danmakuStyleRows(0))
        assertEquals(DanmakuStyleRows(shadow = true, stroke = false, projection = false), danmakuStyleRows(1))
        assertEquals(DanmakuStyleRows(shadow = false, stroke = true, projection = false), danmakuStyleRows(2))
        assertEquals(DanmakuStyleRows(shadow = false, stroke = false, projection = true), danmakuStyleRows(3))
    }

    @Test
    fun colorHintHidesDefaultMode() {
        assertFalse(danmakuColorHintVisible(1))
        assertTrue(danmakuColorHintVisible(2))
    }

    @Test
    fun styleAndColorIndexesMatchChips() {
        assertEquals(2, danmakuStyleIndex(9))
        assertEquals(0, danmakuColorIndex(1))
        assertEquals(1, danmakuColorIndex(2))
        assertEquals(2, danmakuColorIndex(3))
        assertEquals(2, danmakuStyleMode(2))
        assertEquals(1, danmakuColorMode(0))
        assertEquals(3, danmakuColorMode(2))
    }

    @Test
    fun zeroLinesUseAutoLabel() {
        assertEquals("自动", danmakuLinesLabel(0, "自动"))
        assertEquals("4", danmakuLinesLabel(4, "自动"))
    }

    @Test
    fun sliderStepsExcludeEndpoints() {
        assertEquals(24, sliderSteps(0.5f, 3.0f, 0.1f))
        assertEquals(599, sliderSteps(-300000f, 300000f, 1000f))
    }
}
