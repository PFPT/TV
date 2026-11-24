package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class VodControlConfigTest {

    @Test
    fun speedBelowRangeDisplaysHalf() {
        assertEquals(0.5f, sanitizeVodControlConfig(config(speed = 0.25f)).speed, 0.001f)
    }

    @Test
    fun speedAboveRangeDisplaysHalf() {
        assertEquals(0.5f, sanitizeVodControlConfig(config(speed = 6f)).speed, 0.001f)
    }

    @Test
    fun nonFiniteSpeedDisplaysHalf() {
        assertEquals(0.5f, sanitizeVodControlConfig(config(speed = Float.NaN)).speed, 0.001f)
        assertEquals(0.5f, sanitizeVodControlConfig(config(speed = Float.POSITIVE_INFINITY)).speed, 0.001f)
    }

    @Test
    fun speedOnStepStays() {
        assertEquals(1f, sanitizeVodControlConfig(config(speed = 1f)).speed, 0.001f)
        assertEquals(1.25f, sanitizeVodControlConfig(config(speed = 1.25f)).speed, 0.001f)
    }

    @Test
    fun speedBetweenStepsRoundsToHigherStepOnATie() {
        assertEquals(1.25f, sanitizeVodControlConfig(config(speed = 1.125f)).speed, 0.001f)
    }

    @Test
    fun scaleIndexOutsideLabelsBecomesMinusOne() {
        val labels = listOf("0", "1", "2", "3", "4")
        assertEquals(-1, sanitizeVodControlConfig(config(scaleLabels = labels, scaleIndex = 5)).scaleIndex)
        assertEquals(-1, sanitizeVodControlConfig(config(scaleLabels = labels, scaleIndex = -1)).scaleIndex)
    }

    @Test
    fun hiddenParseClearsNamesAndIndex() {
        val result = sanitizeVodControlConfig(config(showParse = false, parseNames = listOf("a"), parseIndex = 0))
        assertEquals(false, result.showParse)
        assertEquals(emptyList<String>(), result.parseNames)
        assertEquals(-1, result.parseIndex)
    }

    @Test
    fun visibleEmptyParseKeepsTheSectionAndClearsTheIndex() {
        val result = sanitizeVodControlConfig(config(showParse = true, parseNames = emptyList(), parseIndex = 0))
        assertEquals(true, result.showParse)
        assertEquals(emptyList<String>(), result.parseNames)
        assertEquals(-1, result.parseIndex)
    }

    @Test
    fun trackTitleFollowsAnyVisibleTrack() {
        assertEquals(false, sanitizeVodControlConfig(config()).showTrackTitle)
        assertEquals(true, sanitizeVodControlConfig(config(showText = true)).showTrackTitle)
    }

    private fun config(
        speed: Float = 1f,
        showParse: Boolean = false,
        parseNames: List<String> = emptyList(),
        parseIndex: Int = -1,
        scaleLabels: List<String> = listOf("0", "1", "2", "3", "4"),
        scaleIndex: Int = 0,
        showText: Boolean = false,
        showAudio: Boolean = false,
        showVideo: Boolean = false,
    ): VodControlConfig {
        return VodControlConfig(
            speed = speed,
            showParse = showParse,
            parseNames = parseNames,
            parseIndex = parseIndex,
            scaleLabels = scaleLabels,
            scaleIndex = scaleIndex,
            playerLabel = "EXO",
            decodeLabel = "硬解",
            openingLabel = "片头",
            endingLabel = "片尾",
            showEdition = false,
            showChapter = false,
            showText = showText,
            showAudio = showAudio,
            showVideo = showVideo,
            showTrackTitle = true,
            showDanmaku = false,
            repeatOn = false,
            timerOn = false,
        )
    }
}