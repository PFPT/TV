package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.bean.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VodDownloadResolverTest {

    @Test
    fun fromPlayerRejectsEmpty() {
        assertNull(VodDownloadResolver.fromPlayer(null, null))
        assertNull(VodDownloadResolver.fromPlayer("", emptyMap()))
    }

    @Test
    fun fromPlayerReturnsReady() {
        val headers = mapOf("UA" to "x")
        val ready = VodDownloadResolver.fromPlayer("https://a", headers)
        assertEquals("https://a", ready!!.url)
        assertEquals(headers, ready.headers)
    }

    @Test
    fun fromResultMessage() {
        val result = Result.error("oops")
        val outcome = VodDownloadResolver.fromResult(result)
        assertTrue(outcome is DownloadResolveOutcome.Message)
        assertEquals("oops", (outcome as DownloadResolveOutcome.Message).text)
    }

    @Test
    fun fromResultEmptyUrl() {
        val result = Result.empty()
        val outcome = VodDownloadResolver.fromResult(result)
        assertTrue(outcome is DownloadResolveOutcome.EmptyUrl)
    }

    @Test
    fun buildFileNameJoinsParts() {
        assertEquals("Show-Ep1", VodDownloadResolver.buildFileName("Show", "Ep1"))
        assertEquals("Show", VodDownloadResolver.buildFileName("Show", ""))
        assertEquals("Ep1", VodDownloadResolver.buildFileName("", "Ep1"))
    }
}
