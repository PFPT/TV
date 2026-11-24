package com.pfpt.tv.ui.vod.compose

import androidx.media3.common.C
import com.pfpt.tv.setting.PlayerSetting
import com.pfpt.tv.utils.Util
import org.junit.Assert.assertEquals
import org.junit.Test

class VodConfigSheetsTest {

    @Test
    fun unsetEditionDurationKeepsLabel() {
        assertEquals("Director", editionRowLabel("Director", C.TIME_UNSET))
    }

    @Test
    fun editionDurationAppendsTime() {
        val durationUs = 65_000_000L
        assertEquals("Director [" + Util.timeMs(durationUs / 1000) + "]", editionRowLabel("Director", durationUs))
    }

    @Test
    fun unsetChapterTimeKeepsLabel() {
        assertEquals("Opening", chapterRowLabel("Opening", C.TIME_UNSET))
    }

    @Test
    fun chapterTimeAppendsTime() {
        val timeUs = 90_000_000L
        assertEquals("Opening [" + Util.timeMs(timeUs / 1000) + "]", chapterRowLabel("Opening", timeUs))
    }

    @Test
    fun danmakuSearchNeedsMetadataAndUrl() {
        assertEquals(false, danmakuShowSearch(false, "https://api"))
        assertEquals(false, danmakuShowSearch(true, ""))
        assertEquals(false, danmakuShowSearch(true, null))
        assertEquals(true, danmakuShowSearch(true, "https://api"))
    }

    @Test
    fun engineZeroSelectsExoOnly() {
        val selection = engineSelection(PlayerSetting.ENGINE_EXO)
        assertEquals(true, selection.exo)
        assertEquals(false, selection.mpv)
    }

    @Test
    fun engineOneSelectsMpvOnly() {
        val selection = engineSelection(PlayerSetting.ENGINE_MPV)
        assertEquals(false, selection.exo)
        assertEquals(true, selection.mpv)
    }
}
