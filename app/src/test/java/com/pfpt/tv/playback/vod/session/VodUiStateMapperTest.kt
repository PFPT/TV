package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.bean.History
import com.pfpt.tv.playback.vod.VodPlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VodUiStateMapperTest {

    @Test
    fun mapsNullState() {
        val ui = VodUiStateMapper.from(null, true, "err", null)
        assertTrue(ui.kept)
        assertEquals("err", ui.errorMessage)
        assertEquals("", ui.title)
    }

    @Test
    fun mapsHistoryTitleAndMarks() {
        val state = VodPlaybackState()
        val history = History()
        history.vodName = "Title"
        history.opening = 12L
        history.ending = 34L
        state.history = history
        val ui = VodUiStateMapper.from(state, false, null, VodDownloadReadySummary("f", "u"))
        assertEquals("Title", ui.title)
        assertEquals(12L, ui.opening)
        assertEquals(34L, ui.ending)
        assertNull(ui.errorMessage)
        assertEquals("f", ui.downloadReady!!.fileName)
    }

    @Test
    fun mapsSiteAndRemarkFromUpdate() {
        val ui = VodUiStateMapper.from(
            null,
            kept = true,
            errorMessage = null,
            downloadReady = null,
            siteName = "S",
            remark = "R",
        )
        assertTrue(ui.kept)
        assertEquals("S", ui.siteName)
        assertEquals("R", ui.remark)
    }

    @Test
    fun preservesSiteAndRemarkWhenStatePresent() {
        val state = VodPlaybackState()
        val history = History()
        history.vodName = "Title"
        state.history = history
        val ui = VodUiStateMapper.from(
            state,
            kept = false,
            errorMessage = null,
            downloadReady = null,
            siteName = "S",
            remark = "R",
        )
        assertEquals("Title", ui.title)
        assertEquals("S", ui.siteName)
        assertEquals("R", ui.remark)
    }

    @Test
    fun twoFlagsMarksSelectedCorrectly() {
        val state = VodUiState(
            flags = listOf(
                VodFlagItem("Line A", false),
                VodFlagItem("Line B", true),
            ),
        )
        assertEquals(false, state.flags[0].selected)
        assertEquals("Line B", state.flags[1].name)
        assertEquals(true, state.flags[1].selected)
        val selected = state.flags.filter { it.selected }
        assertEquals(1, selected.size)
    }

    @Test
    fun mapperKeepsCatalogSelection() {
        val ui = VodUiStateMapper.from(
            null,
            kept = false,
            errorMessage = null,
            downloadReady = null,
            flags = listOf(VodFlagItem("Line A", false), VodFlagItem("Line B", true)),
            episodes = listOf(VodEpisodeItem("EP1", true, "http://ep")),
            qualities = listOf("1080", "720"),
            qualityIndex = 1,
        )
        assertEquals(true, ui.flags[1].selected)
        assertEquals(false, ui.flags[0].selected)
        assertEquals("EP1", ui.episodes[0].name)
        assertEquals(1, ui.qualityIndex)
    }

    @Test
    fun playbackChromeDefaultsAndCopy() {
        val ui = VodUiState()
        assertEquals(false, ui.controlsVisible)
        assertEquals(false, ui.locked)
        assertEquals(false, ui.playing)
        assertEquals(0L, ui.positionMs)
        assertEquals(0L, ui.durationMs)
        assertEquals(1f, ui.speed, 0f)
        assertEquals(false, ui.fullscreen)
        val copied = ui.copy(
            controlsVisible = true,
            locked = true,
            playing = true,
            positionMs = 1500L,
            durationMs = 9000L,
            speed = 1.5f,
            fullscreen = true,
        )
        assertEquals(true, copied.controlsVisible)
        assertEquals(true, copied.locked)
        assertEquals(true, copied.playing)
        assertEquals(1500L, copied.positionMs)
        assertEquals(9000L, copied.durationMs)
        assertEquals(1.5f, copied.speed, 0f)
        assertEquals(true, copied.fullscreen)
        val mapped = VodUiStateMapper.from(null, copied.kept, copied.errorMessage, copied.downloadReady)
        val restored = mapped.copy(
            title = mapped.title.ifEmpty { copied.title },
            controlsVisible = copied.controlsVisible,
            locked = copied.locked,
            playing = copied.playing,
            positionMs = copied.positionMs,
            durationMs = copied.durationMs,
            speed = copied.speed,
            fullscreen = copied.fullscreen,
        )
        assertEquals(false, mapped.controlsVisible)
        assertEquals(0L, mapped.positionMs)
        assertEquals(true, restored.controlsVisible)
        assertEquals(1500L, restored.positionMs)
        assertEquals(1.5f, restored.speed, 0f)
    }
}
