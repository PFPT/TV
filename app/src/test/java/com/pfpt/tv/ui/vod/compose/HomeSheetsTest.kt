package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSheetsTest {

    @Test
    fun hiddenSitesStayOut() {
        val rows = homeSiteRows(
            listOf(
                HomeSiteInput("甲", hidden = true, selected = false, changeable = true),
                HomeSiteInput("乙", hidden = false, selected = true, changeable = false),
            ),
        )
        assertEquals(listOf(HomeSiteRow("乙", selected = true, changeable = false)), rows)
    }

    @Test
    fun readOnlyHistoryKeepsTheFirstConfig() {
        assertEquals(listOf("当前", "旧的"), homeHistoryNames(listOf("当前", "旧的"), readOnly = true))
    }

    @Test
    fun editableHistoryDropsTheFirstConfig() {
        assertEquals(listOf("旧的"), homeHistoryNames(listOf("当前", "旧的"), readOnly = false))
        assertEquals(emptyList<String>(), homeHistoryNames(emptyList(), readOnly = false))
    }

    @Test
    fun blankLinkDoesNotPlay() {
        assertFalse(homeLinkReady("  "))
        assertTrue(homeLinkReady(" https://a "))
    }

    @Test
    fun filterSelectionUsesTheFirstSelectedValue() {
        assertEquals(2, firstSelected(listOf(false, false, true)))
        assertEquals(-1, firstSelected(listOf(false, false)))
    }
}
