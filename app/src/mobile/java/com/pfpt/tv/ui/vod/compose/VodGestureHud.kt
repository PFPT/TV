package com.pfpt.tv.ui.vod.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodUiState
import com.pfpt.tv.setting.PlayerSetting
import java.util.Locale

@Composable
fun VodGestureHud(
    state: VodUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val chipModifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 24.dp)
        GestureHudChip(
            visible = state.gestureBrightVisible,
            progress = state.gestureBrightProgress,
            iconRes = brightIcon(state.gestureBrightProgress),
            modifier = chipModifier,
        )
        GestureHudChip(
            visible = state.gestureVolumeVisible,
            progress = state.gestureVolumeProgress,
            iconRes = volumeIcon(state.gestureVolumeProgress),
            modifier = chipModifier,
        )
        SeekHudChip(
            visible = state.gestureSeekVisible,
            forward = state.gestureSeekForward,
            time = state.gestureSeekTime,
            modifier = chipModifier,
        )
        SpeedHudChip(
            visible = state.gestureSpeedVisible,
            speed = if (state.gestureSpeedVisible) PlayerSetting.getSpeed() else state.speed,
            modifier = chipModifier,
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
        PlayerHudCapsule {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0, 100) / 100f },
                modifier = Modifier
                    .width(120.dp)
                    .height(VodPlayerChrome.TrackHeight),
                color = VodPlayerChrome.TrackActive,
                trackColor = VodPlayerChrome.TrackInactive,
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
        PlayerHudCapsule {
            Icon(
                painter = painterResource(
                    if (forward) R.drawable.ic_widget_forward else R.drawable.ic_widget_rewind,
                ),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = time,
                color = Color.White,
                style = TextStyle(
                    fontSize = VodPlayerChrome.HudLabelSize,
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

@Composable
private fun SpeedHudChip(
    visible: Boolean,
    speed: Float,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        val transition = rememberInfiniteTransition(label = "speedHud")
        val pulse by transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "speedHudPulse",
        )
        PlayerHudCapsule(
            modifier = Modifier.graphicsLayer {
                scaleX = pulse
                scaleY = pulse
                alpha = 0.85f + (pulse - 0.92f) / 0.08f * 0.15f
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_widget_forward),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = String.format(Locale.getDefault(), "%.1fx", speed),
                color = Color.White,
                style = TextStyle(
                    fontSize = VodPlayerChrome.HudLabelSize,
                    fontFeatureSettings = "tnum",
                ),
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
