package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.bean.Episode
import com.pfpt.tv.bean.Flag
import com.pfpt.tv.bean.History
import com.pfpt.tv.bean.Result
import com.pfpt.tv.bean.Url
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
        assertTrue(ui.flags.isEmpty())
        assertTrue(ui.episodes.isEmpty())
        assertTrue(ui.qualities.isEmpty())
        assertEquals(0, ui.qualityIndex)
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
    fun derivesFlagsFromPlaybackState() {
        val state = VodPlaybackState()
        val flagA = Flag("Line A")
        val flagB = Flag("Line B")
        flagB.setSelected(flagB)
        flagA.setSelected(flagB)
        state.getFlags().add(flagA)
        state.getFlags().add(flagB)
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertEquals(2, ui.flags.size)
        assertEquals("Line A", ui.flags[0].name)
        assertEquals(false, ui.flags[0].selected)
        assertEquals("Line B", ui.flags[1].name)
        assertEquals(true, ui.flags[1].selected)
    }

    @Test
    fun derivesEpisodesDescPlusNameFromSelectedFlag() {
        val state = VodPlaybackState()
        val flag = Flag("line")
        flag.setSelected(flag)
        val episode = Episode.create("Name", "Desc", "http://ep")
        episode.setSelected(true)
        flag.getEpisodes().add(episode)
        state.getFlags().add(flag)
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertEquals(1, ui.episodes.size)
        assertEquals("DescName", ui.episodes[0].name)
        assertEquals(true, ui.episodes[0].selected)
        assertEquals("http://ep", ui.episodes[0].url)
    }

    @Test
    fun derivesMultiQualityLabelsAndIndex() {
        val state = VodPlaybackState()
        val flag = Flag("line")
        flag.setSelected(flag)
        val episode = Episode.create("Name", "Desc", "http://ep")
        episode.setSelected(true)
        flag.getEpisodes().add(episode)
        state.getFlags().add(flag)
        state.setQuality(resultWithUrl(Url.create().add("1080", "u1").add("720", "u2").set(1)))
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertEquals(listOf("1080", "720"), ui.qualities)
        assertEquals(1, ui.qualityIndex)
    }

    @Test
    fun multiQualityWithoutSelectedEpisodeYieldsEmptyQualities() {
        val state = VodPlaybackState()
        state.setQuality(resultWithUrl(Url.create().add("1080", "u1").add("720", "u2").set(1)))
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertTrue(ui.qualities.isEmpty())
        assertEquals(0, ui.qualityIndex)
    }

    @Test
    fun singleQualityYieldsEmptyQualities() {
        val state = VodPlaybackState()
        state.setQuality(resultWithUrl(Url.create().add("1080", "u1")))
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertTrue(ui.qualities.isEmpty())
        assertEquals(0, ui.qualityIndex)
    }

    @Test
    fun noFlagsYieldsEmptyEpisodes() {
        val state = VodPlaybackState()
        val ui = VodUiStateMapper.from(state, false, null, null)
        assertTrue(ui.episodes.isEmpty())
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
        assertEquals(false, ui.danmakuOn)
        assertEquals(false, ui.showDanmaku)
        val copied = ui.copy(
            controlsVisible = true,
            locked = true,
            playing = true,
            positionMs = 1500L,
            durationMs = 9000L,
            speed = 1.5f,
            fullscreen = true,
            danmakuOn = true,
            showDanmaku = true,
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
            danmakuOn = copied.danmakuOn,
            showDanmaku = copied.showDanmaku,
        )
        assertEquals(false, mapped.controlsVisible)
        assertEquals(0L, mapped.positionMs)
        assertEquals(false, mapped.showDanmaku)
        assertEquals(true, restored.controlsVisible)
        assertEquals(1500L, restored.positionMs)
        assertEquals(1.5f, restored.speed, 0f)
        assertEquals(true, restored.danmakuOn)
        assertEquals(true, restored.showDanmaku)
    }

    private fun resultWithUrl(url: Url): Result {
        val result = Result.empty()
        val field = Result::class.java.getDeclaredField("url")
        field.isAccessible = true
        field.set(result, url)
        return result
    }
}
