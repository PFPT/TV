package com.pfpt.tv.server

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanTokenTest {

    @Test
    fun queryHeaderAndCookieAuthenticate() {
        val token = "abc123"
        assertTrue(LanToken.accept(token, mapOf("token" to token), emptyMap()))
        assertTrue(LanToken.accept(token, emptyMap(), mapOf("x-tv-token" to token)))
        assertTrue(LanToken.accept(token, emptyMap(), mapOf("cookie" to "other=1; tv_token=$token")))
        assertFalse(LanToken.accept(token, mapOf("token" to "nope"), emptyMap()))
        assertFalse(LanToken.accept(token, emptyMap(), emptyMap()))
    }

    @Test
    fun withAndOriginKeepCastAndQrUrls() {
        assertEquals("http://10.0.0.8:9978?tab=3&token=abc", LanToken.with("http://10.0.0.8:9978?tab=3", "abc"))
        assertEquals("http://10.0.0.8:9978/file/a.mp4?token=abc", LanToken.with("http://10.0.0.8:9978/file/a.mp4", "abc"))
        assertEquals("http://10.0.0.8:9978", LanToken.origin("http://10.0.0.8:9978/?tab=2&token=abc"))
        assertEquals("abc", LanToken.query("http://10.0.0.8:9978?tab=2&token=abc", "token"))
        assertNull(LanToken.query("http://10.0.0.8:9978/device", "token"))
    }

    @Test
    fun peerTokenIsAppendedToAction() {
        LanToken.pair("tv-1", "secret")
        assertEquals(
            "http://10.0.0.9:9978/action?do=cast&token=secret",
            LanToken.action("http://10.0.0.9:9978", "tv-1", "/action?do=cast")
        )
        assertEquals("http://10.0.0.9:9978/action?do=cast", LanToken.action("http://10.0.0.9:9978", "missing", "/action?do=cast"))
    }
}
