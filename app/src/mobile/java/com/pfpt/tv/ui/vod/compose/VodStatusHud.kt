package com.pfpt.tv.ui.vod.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.playback.vod.session.VodUiState
import com.pfpt.tv.utils.Util
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HudBg = Color(0x80000000)
private val HudShape = RoundedCornerShape(28.dp)
private val ClockFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
private val TextShadow = Shadow(
    color = Color(0xCC000000),
    offset = Offset(0f, 1f),
    blurRadius = 3f,
)

@Composable
fun VodStatusHud(
    state: VodUiState,
    modifier: Modifier = Modifier,
) {
    val error = state.errorMessage?.takeIf { it.isNotEmpty() }
    val playInfoVisible = state.fullscreen && !(state.controlsVisible && !state.locked)
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
        ) {
            Text(
                text = error.orEmpty(),
                color = Color.White,
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .clip(HudShape)
                    .background(HudBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        AnimatedVisibility(
            visible = playInfoVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 16.dp),
        ) {
            PlayInfoColumn(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
            )
        }
    }
}

@Composable
private fun PlayInfoColumn(
    positionMs: Long,
    durationMs: Long,
) {
    var clock by remember { mutableStateOf(ClockFormat.format(LocalDateTime.now())) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = ClockFormat.format(LocalDateTime.now())
            delay(1000)
        }
    }
    val current = Util.timeMs(positionMs.coerceAtLeast(0L))
    val total = Util.timeMs(durationMs.coerceAtLeast(0L))
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = clock,
            color = Color.White,
            style = TextStyle(
                fontSize = 14.sp,
                fontFeatureSettings = "tnum",
                shadow = TextShadow,
            ),
        )
        Text(
            text = "$current / $total",
            color = Color.White,
            style = TextStyle(
                fontSize = 12.sp,
                fontFeatureSettings = "tnum",
                shadow = TextShadow,
            ),
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
