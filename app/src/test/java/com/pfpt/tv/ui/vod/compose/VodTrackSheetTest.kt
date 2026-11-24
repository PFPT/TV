package com.pfpt.tv.ui.vod.compose

import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Test

class VodTrackSheetTest {

    @Test
    fun vodTextWithTrackShowsAllActions() {
        val actions = vodTrackActions(C.TRACK_TYPE_TEXT, isVod = true, haveTrack = true)
        assertEquals(true, actions.showChoose)
        assertEquals(true, actions.showSubtitle)
        assertEquals(true, actions.showOffset)
    }

    @Test
    fun audioWithTrackShowsOnlyOffset() {
        val actions = vodTrackActions(C.TRACK_TYPE_AUDIO, isVod = true, haveTrack = true)
        assertEquals(false, actions.showChoose)
        assertEquals(false, actions.showSubtitle)
        assertEquals(true, actions.showOffset)
    }

    @Test
    fun videoTrackShowsNoActions() {
        val actions = vodTrackActions(C.TRACK_TYPE_VIDEO, isVod = true, haveTrack = true)
        assertEquals(false, actions.showChoose)
        assertEquals(false, actions.showSubtitle)
        assertEquals(false, actions.showOffset)
    }

    @Test
    fun missingTrackShowsNoActions() {
        val text = vodTrackActions(C.TRACK_TYPE_TEXT, isVod = false, haveTrack = false)
        assertEquals(false, text.showChoose)
        assertEquals(false, text.showSubtitle)
        assertEquals(false, text.showOffset)
        val audio = vodTrackActions(C.TRACK_TYPE_AUDIO, isVod = true, haveTrack = false)
        assertEquals(false, audio.showOffset)
    }
}
