package com.pfpt.tv.ui.vod.compose

import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

private const val SCALE_COOLDOWN_MS = 500L
private const val EDGE_DP = 24

private enum class GestureWatch {
    Up,
    Drag,
    Pinch,
}

@Composable
fun VodGestureLayer(
    locked: Boolean,
    onBrightness: (Float) -> Unit,
    onVolume: (Float) -> Unit,
    onSeekPreview: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit,
    onSpeedUp: () -> Unit,
    onSpeedEnd: () -> Unit,
    onFlingUp: () -> Unit,
    onFlingDown: () -> Unit,
    onScale: (Float, Float, Float) -> Unit,
    onTouchEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lockedState by rememberUpdatedState(locked)
    val brightnessState by rememberUpdatedState(onBrightness)
    val volumeState by rememberUpdatedState(onVolume)
    val seekPreviewState by rememberUpdatedState(onSeekPreview)
    val seekEndState by rememberUpdatedState(onSeekEnd)
    val singleTapState by rememberUpdatedState(onSingleTap)
    val doubleTapState by rememberUpdatedState(onDoubleTap)
    val speedUpState by rememberUpdatedState(onSpeedUp)
    val speedEndState by rememberUpdatedState(onSpeedEnd)
    val flingUpState by rememberUpdatedState(onFlingUp)
    val flingDownState by rememberUpdatedState(onFlingDown)
    val scaleState by rememberUpdatedState(onScale)
    val touchEndState by rememberUpdatedState(onTouchEnd)
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    val scope = this
                    var tapToken = 0
                    var lastTapAt = 0L
                    var lastTap = Offset.Zero
                    var scaleBlockedUntil = 0L
                    val edge = EDGE_DP.dp.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        tapToken += 1
                        val origin = down.position
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        val xFraction = (origin.x / width).coerceIn(0f, 1f)
                        val atEdge = origin.x < edge || origin.x > width - edge || origin.y < edge || origin.y > size.height.toFloat() - edge
                        val suppressed = atEdge || SystemClock.uptimeMillis() < scaleBlockedUntil
                        var state = VodGestureState(locked = lockedState)
                        var totalX = 0f
                        var totalY = 0f
                        var pinching = false
                        var speed = false
                        val pointerId = down.id

                        fun publish(previous: VodGestureState, next: VodGestureState) {
                            when (next.kind) {
                                VodGestureKind.Brightness -> if (previous.kind != next.kind || previous.delta != next.delta) brightnessState(next.delta)
                                VodGestureKind.Volume -> if (previous.kind != next.kind || previous.delta != next.delta) volumeState(next.delta)
                                VodGestureKind.Seek -> if (previous.kind != next.kind || previous.seekMs != next.seekMs) seekPreviewState(next.seekMs)
                                VodGestureKind.None -> Unit
                            }
                        }

                        fun dragTo(x: Float, y: Float) {
                            totalX = x - origin.x
                            totalY = y - origin.y
                            if (speed || pinching || suppressed) return
                            val next = state.copy(locked = lockedState).onDrag(totalX, totalY, xFraction, false)
                            publish(state, next)
                            state = next
                        }

                        fun pinch(changes: List<PointerInputChange>) {
                            if (lockedState || state.decided || suppressed || changes.size < 2) return
                            val a = changes[0]
                            val b = changes[1]
                            val previous = hypot(
                                (a.previousPosition.x - b.previousPosition.x).toDouble(),
                                (a.previousPosition.y - b.previousPosition.y).toDouble(),
                            ).toFloat()
                            if (previous < 1f) return
                            val current = hypot(
                                (a.position.x - b.position.x).toDouble(),
                                (a.position.y - b.position.y).toDouble(),
                            ).toFloat()
                            val factor = current / previous
                            if (state.onPinch(factor).scale == state.scale) return
                            scaleState(factor, (a.position.x + b.position.x) / 2f, (a.position.y + b.position.y) / 2f)
                        }

                        val watch = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size >= 2) {
                                    pinching = true
                                    pinch(pressed)
                                    event.changes.forEach { it.consume() }
                                    return@withTimeoutOrNull GestureWatch.Pinch
                                }
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull()
                                if (change == null || !change.pressed) {
                                    if (change != null) dragTo(change.position.x, change.position.y)
                                    event.changes.forEach { it.consume() }
                                    return@withTimeoutOrNull GestureWatch.Up
                                }
                                dragTo(change.position.x, change.position.y)
                                change.consume()
                                if (state.decided) return@withTimeoutOrNull GestureWatch.Drag
                            }
                        }
                        if (watch == null && !lockedState && !suppressed && !state.decided && !pinching) {
                            speed = true
                            speedUpState()
                        }
                        if (watch != GestureWatch.Up) {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size >= 2) {
                                    pinching = true
                                    pinch(pressed)
                                } else {
                                    val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull()
                                    if (change != null) dragTo(change.position.x, change.position.y)
                                }
                                event.changes.forEach { it.consume() }
                                if (pressed.isEmpty()) break
                            }
                        } else if (!suppressed && !state.decided && !pinching && !speed) {
                            val now = SystemClock.uptimeMillis()
                            val slop = viewConfiguration.touchSlop
                            val near = abs(origin.x - lastTap.x) < slop && abs(origin.y - lastTap.y) < slop
                            if (lastTapAt != 0L && now - lastTapAt < viewConfiguration.doubleTapTimeoutMillis && near) {
                                lastTapAt = 0L
                                doubleTapState()
                            } else {
                                lastTapAt = now
                                lastTap = origin
                                val token = tapToken
                                scope.launch {
                                    delay(viewConfiguration.doubleTapTimeoutMillis)
                                    if (token == tapToken) singleTapState()
                                }
                            }
                        }
                        if (pinching) scaleBlockedUntil = SystemClock.uptimeMillis() + SCALE_COOLDOWN_MS
                        touchEndState()
                        if (speed) speedEndState()
                        if (state.kind == VodGestureKind.Seek) seekEndState()
                        if (!speed && !pinching && !suppressed) {
                            when (state.fling(totalX, totalY, xFraction, false)) {
                                VodFling.Up -> flingUpState()
                                VodFling.Down -> flingDownState()
                                VodFling.None -> Unit
                            }
                        }
                    }
                }
            },
    )
}
