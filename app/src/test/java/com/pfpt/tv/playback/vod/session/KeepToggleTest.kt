package com.pfpt.tv.playback.vod.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepToggleTest {

    @Test
    fun toggleAddsWhenMissing() {
        val store = mutableSetOf<String>()
        val toggle = KeepToggle(
            exists = { store.contains(it) },
            create = { store.add(it.key) },
            delete = { store.remove(it) },
        )
        val result = toggle.toggle(KeepToggleRequest("k1", 1, "n", "p", "s"))
        assertTrue(result.kept)
        assertFalse(result.removed)
        assertTrue(store.contains("k1"))
    }

    @Test
    fun toggleRemovesWhenPresent() {
        val store = mutableSetOf("k1")
        val toggle = KeepToggle(
            exists = { store.contains(it) },
            create = { store.add(it.key) },
            delete = { store.remove(it) },
        )
        val result = toggle.toggle(KeepToggleRequest("k1", 1, "n", "p", "s"))
        assertFalse(result.kept)
        assertTrue(result.removed)
        assertFalse(store.contains("k1"))
    }

    @Test
    fun toggleRoundTrip() {
        val store = mutableSetOf<String>()
        val toggle = KeepToggle(
            exists = { store.contains(it) },
            create = { store.add(it.key) },
            delete = { store.remove(it) },
        )
        val req = KeepToggleRequest("k2", 2, "a", "b", "c")
        assertEquals(true, toggle.toggle(req).kept)
        assertEquals(false, toggle.toggle(req).kept)
        assertEquals(true, toggle.toggle(req).kept)
    }
}
