package com.pfpt.tv.ui.vod.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class VodGestureStateTest {

    @Test
    fun ignoresHorizontalSwipeWhenLocked() {
        val s0 = VodGestureState(locked = true)
        val s1 = s0.onDrag(dx = 80f, dy = 0f, xFraction = 0.5f)
        assertEquals(VodGestureKind.None, s1.kind)
    }

    @Test
    fun leftSideDragChangesBrightnessWhenUnlocked() {
        val s0 = VodGestureState(locked = false)
        val s1 = s0.onDrag(dx = 0f, dy = -40f, xFraction = 0.2f)
        assertEquals(VodGestureKind.Brightness, s1.kind)
    }

    @Test
    fun leftSideDragUpReportsPositiveBrightnessDelta() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 0f, dy = -40f, xFraction = 0.2f)
        assertEquals(VodGestureKind.Brightness, s1.kind)
        assertEquals(40f, s1.delta)
    }

    @Test
    fun rightSideDragChangesVolumeWhenUnlocked() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 0f, dy = -40f, xFraction = 0.8f)
        assertEquals(VodGestureKind.Volume, s1.kind)
        assertEquals(40f, s1.delta)
    }

    @Test
    fun centerHorizontalDragSeeks() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 80f, dy = 0f, xFraction = 0.5f)
        assertEquals(VodGestureKind.Seek, s1.kind)
        assertEquals(4000L, s1.seekMs)
    }

    @Test
    fun horizontalDragOnLeftStillSeeks() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 80f, dy = -10f, xFraction = 0.2f)
        assertEquals(VodGestureKind.Seek, s1.kind)
    }

    @Test
    fun centerVerticalDragIsNone() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 0f, dy = -80f, xFraction = 0.5f)
        assertEquals(VodGestureKind.None, s1.kind)
    }

    @Test
    fun edgeDragIsNone() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 80f, dy = 0f, xFraction = 0.5f, atEdge = true)
        assertEquals(VodGestureKind.None, s1.kind)
    }

    @Test
    fun shortDragDoesNotCommit() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 10f, dy = 0f, xFraction = 0.5f)
        assertEquals(VodGestureKind.None, s1.kind)
        assertEquals(false, s1.decided)
    }

    @Test
    fun committedKindDoesNotSwitch() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 0f, dy = -40f, xFraction = 0.2f)
        val s2 = s1.onDrag(dx = 200f, dy = -40f, xFraction = 0.2f)
        assertEquals(VodGestureKind.Brightness, s2.kind)
        assertEquals(40f, s2.delta)
    }

    @Test
    fun pinchIgnoredWhenLockedOrDragging() {
        assertEquals(2f, VodGestureState(locked = true, scale = 2f).onPinch(2f).scale)
        val dragging = VodGestureState(locked = false).onDrag(dx = 0f, dy = -40f, xFraction = 0.2f)
        assertEquals(1f, dragging.onPinch(2f).scale)
    }

    @Test
    fun pinchClampsScale() {
        assertEquals(5f, VodGestureState(scale = 2f).onPinch(3f).scale)
        assertEquals(1f, VodGestureState(scale = 1f).onPinch(0.2f).scale)
        assertEquals(1f, VodGestureState(scale = 3f).resetScale().scale)
    }

    @Test
    fun centerFlingUpWhenUnlocked() {
        val s1 = VodGestureState(locked = false).onDrag(dx = 0f, dy = -120f, xFraction = 0.5f)
        assertEquals(VodFling.Up, s1.fling(dx = 0f, dy = -120f, xFraction = 0.5f))
    }

    @Test
    fun flingIgnoredWhenLockedOrOnSide() {
        assertEquals(VodFling.None, VodGestureState(locked = true).fling(dx = 0f, dy = -120f, xFraction = 0.5f))
        assertEquals(VodFling.None, VodGestureState(locked = false).fling(dx = 0f, dy = -120f, xFraction = 0.1f))
        assertEquals(VodFling.Down, VodGestureState(locked = false).fling(dx = 0f, dy = 120f, xFraction = 0.5f))
    }
}
