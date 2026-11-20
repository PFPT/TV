package com.pfpt.tv.ui.vod.compose

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

enum class VodGestureKind {
    None,
    Brightness,
    Volume,
    Seek,
}

enum class VodFling {
    None,
    Up,
    Down,
}

data class VodGestureState(
    val locked: Boolean = false,
    val kind: VodGestureKind = VodGestureKind.None,
    val delta: Float = 0f,
    val seekMs: Long = 0L,
    val scale: Float = 1f,
    val decided: Boolean = false,
) {
    fun onDrag(
        dx: Float,
        dy: Float,
        xFraction: Float,
        atEdge: Boolean = false,
    ): VodGestureState {
        if (locked || atEdge) {
            return copy(kind = VodGestureKind.None, delta = 0f, seekMs = 0L, decided = true)
        }
        if (decided) return track(dx, dy)
        if (hypot(dx.toDouble(), dy.toDouble()) < MIN_DISTANCE) return this
        val adx = abs(dx)
        val ady = abs(dy)
        val x = xFraction.coerceIn(0f, 1f)
        return when {
            adx >= ady -> copy(
                kind = VodGestureKind.Seek,
                delta = dx,
                seekMs = (dx * SEEK_MS_PER_PX).toLong(),
                decided = true,
            )
            x <= SIDE_FRACTION -> copy(
                kind = VodGestureKind.Brightness,
                delta = -dy,
                seekMs = 0L,
                decided = true,
            )
            x >= 1f - SIDE_FRACTION -> copy(
                kind = VodGestureKind.Volume,
                delta = -dy,
                seekMs = 0L,
                decided = true,
            )
            else -> copy(kind = VodGestureKind.None, delta = 0f, seekMs = 0L, decided = true)
        }
    }

    fun onPinch(factor: Float): VodGestureState {
        if (locked || decided || !factor.isFinite() || factor <= 0f) return this
        return copy(scale = (scale * factor).coerceIn(MIN_SCALE, MAX_SCALE))
    }

    fun resetScale(): VodGestureState = copy(scale = 1f)

    fun fling(dx: Float, dy: Float, xFraction: Float, atEdge: Boolean = false): VodFling {
        if (locked || atEdge || (decided && kind != VodGestureKind.None)) return VodFling.None
        val x = xFraction.coerceIn(0f, 1f)
        if (x <= SIDE_FRACTION || x >= 1f - SIDE_FRACTION) return VodFling.None
        val adx = abs(dx).toDouble()
        val ady = abs(dy).toDouble()
        if (adx == 0.0 && ady == 0.0) return VodFling.None
        val angle = atan2(ady, adx) * 180.0 / PI
        if (angle <= FLING_ANGLE) return VodFling.None
        return when {
            -dy > FLING_DISTANCE -> VodFling.Up
            dy > FLING_DISTANCE -> VodFling.Down
            else -> VodFling.None
        }
    }

    private fun track(dx: Float, dy: Float): VodGestureState = when (kind) {
        VodGestureKind.Seek -> copy(delta = dx, seekMs = (dx * SEEK_MS_PER_PX).toLong())
        VodGestureKind.Brightness, VodGestureKind.Volume -> copy(delta = -dy)
        VodGestureKind.None -> this
    }

    companion object {
        const val MIN_DISTANCE = 20f
        const val SIDE_FRACTION = 0.25f
        const val SEEK_MS_PER_PX = 50f
        const val MIN_SCALE = 1f
        const val MAX_SCALE = 5f
        const val FLING_DISTANCE = 100f
        const val FLING_ANGLE = 70.0
    }
}
