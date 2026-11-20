package com.pfpt.tv.ui.vod.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodUiState
import kotlin.math.roundToInt

private val HudBg = Color(0x80000000)
private val TrackColor = Color(0xFF9E9E9E)
private val HudShape = RoundedCornerShape(28.dp)

@Composable
fun VodGestureHud(
    state: VodUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GestureHudChip(
            visible = state.gestureBrightVisible,
            progress = state.gestureBrightProgress,
            iconRes = brightIcon(state.gestureBrightProgress),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )
        GestureHudChip(
            visible = state.gestureVolumeVisible,
            progress = state.gestureVolumeProgress,
            iconRes = volumeIcon(state.gestureVolumeProgress),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )
        SeekHudChip(
            visible = state.gestureSeekVisible,
            forward = state.gestureSeekForward,
            time = state.gestureSeekTime,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )
        SpeedHudIcon(
            visible = state.gestureSpeedVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        )
    }
}

@Composable
private fun GestureHudChip(
    visible: Boolean,
    progress: Int,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .clip(HudShape)
                .background(HudBg)
                .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp),
                color = Color.White,
                trackColor = TrackColor,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
        }
    }
}

@Composable
private fun SeekHudChip(
    visible: Boolean,
    forward: Boolean,
    time: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .clip(HudShape)
                .background(HudBg)
                .padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(
                    if (forward) R.drawable.ic_widget_forward else R.drawable.ic_widget_rewind,
                ),
                contentDescription = null,
                tint = Color.Unspecified,
            )
            Text(
                text = time,
                color = Color.White,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

@Composable
private fun SpeedHudIcon(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        BoxWithConstraints {
            val density = LocalDensity.current
            val travelPx = with(density) { (maxWidth * 0.5f).toPx() }
            val transition = rememberInfiniteTransition(label = "speedHud")
            val offsetX by transition.animateFloat(
                initialValue = -travelPx,
                targetValue = travelPx,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "speedHudOffset",
            )
            Icon(
                painter = painterResource(R.drawable.ic_widget_forward),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.offset { IntOffset(offsetX.roundToInt(), 0) },
            )
        }
    }
}

@DrawableRes
private fun brightIcon(progress: Int): Int = when {
    progress < 35 -> R.drawable.ic_widget_bright_low
    progress < 70 -> R.drawable.ic_widget_bright_medium
    else -> R.drawable.ic_widget_bright_high
}

@DrawableRes
private fun volumeIcon(progress: Int): Int = when {
    progress < 35 -> R.drawable.ic_widget_volume_low
    progress < 70 -> R.drawable.ic_widget_volume_medium
    else -> R.drawable.ic_widget_volume_high
}
