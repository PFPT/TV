package com.pfpt.tv.ui.vod.compose

import android.view.View
import kotlin.math.max
import kotlin.math.min

class VodPlayerScale(private val videoView: View) {
    private var scale = 1f

    fun apply(factor: Float, focusX: Float, focusY: Float) {
        if (factor <= 0f || factor.isNaN() || factor.isInfinite()) return
        scale = min(5f, max(1f, scale * factor))
        videoView.pivotX = focusX
        videoView.pivotY = focusY
        videoView.scaleX = scale
        videoView.scaleY = scale
    }

    fun reset() {
        scale = 1f
        videoView.scaleX = 1f
        videoView.scaleY = 1f
    }
}
