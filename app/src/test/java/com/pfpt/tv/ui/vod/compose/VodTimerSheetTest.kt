package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class VodTimerSheetTest {

    @Test
    fun stoppedTimerShowsChoices() {
        assertEquals(VodTimerContent.Choices, vodTimerContent(false))
    }

    @Test
    fun runningTimerShowsCountdown() {
        assertEquals(VodTimerContent.Countdown, vodTimerContent(true))
    }
}
