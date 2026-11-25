package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class VodPlaybackSheetsTest {

    @Test
    fun snapOffsetAlignsToStep() {
        assertEquals(200f, snapOffset(160f, -10000f, 10000f, 100f), 0.01f)
    }

    @Test
    fun snapOffsetClampsLow() {
        assertEquals(-10000f, snapOffset(-20000f, -10000f, 10000f, 100f), 0.01f)
    }

    @Test
    fun snapOffsetClampsHigh() {
        assertEquals(10000f, snapOffset(20000f, -10000f, 10000f, 100f), 0.01f)
    }

    @Test
    fun zeroOffsetLabelIsSigned() {
        assertEquals("+0.0s", offsetLabel(0f))
    }
}
