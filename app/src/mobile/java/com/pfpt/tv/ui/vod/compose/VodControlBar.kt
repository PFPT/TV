package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodUiState
import com.pfpt.tv.utils.Util
import java.util.Locale

@Composable
fun VodControlTopBar(
    state: VodUiState,
    onBack: () -> Unit,
    onKeep: () -> Unit,
    onMore: () -> Unit,
    onCast: () -> Unit,
    onDanmaku: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VodPlayerChrome.TopScrim)
            .padding(
                start = VodPlayerChrome.BarHorizontal,
                end = VodPlayerChrome.BarHorizontal,
                top = VodPlayerChrome.BarVertical,
                bottom = 16.dp,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlIcon(R.drawable.ic_control_back, onBack)
            Text(
                text = controlHeadline(state),
                color = Color.White,
                fontSize = VodPlayerChrome.TitleSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
            )
            if (state.speed != 1f) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2fx", state.speed),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = VodPlayerChrome.MetaSize,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            if (state.showCast) {
                ControlIcon(R.drawable.ic_control_cast, onCast)
            }
            if (state.showDanmaku) {
                ControlIcon(
                    if (state.danmakuOn) R.drawable.ic_control_danmaku_on else R.drawable.ic_control_danmaku_off,
                    onDanmaku,
                )
            }
            if (!state.fullscreen) {
                ControlIcon(
                    if (state.kept) R.drawable.ic_control_keep_on else R.drawable.ic_control_keep_off,
                    onKeep,
                )
            }
            Text(
                text = stringResource(R.string.control_more),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = VodPlayerChrome.MetaSize,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onMore)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
fun VodControlCenter(
    state: VodUiState,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showSkip = state.episodes.size >= 2
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        if (showSkip) ControlIcon(androidx.media3.ui.R.drawable.exo_icon_previous, onPrev)
        ControlIcon(
            if (state.playing) androidx.media3.ui.R.drawable.exo_icon_pause else androidx.media3.ui.R.drawable.exo_icon_play,
            onPlay,
            VodPlayerChrome.PlayIconSize,
        )
        if (showSkip) ControlIcon(androidx.media3.ui.R.drawable.exo_icon_next, onNext)
    }
}

@Composable
fun VodControlLock(
    locked: Boolean,
    onLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ControlIcon(
        if (locked) R.drawable.ic_control_lock_on else R.drawable.ic_control_lock_off,
        onLock,
        modifier = modifier.padding(end = 8.dp),
    )
}

@Composable
fun VodControlBottomBar(
    state: VodUiState,
    onSeek: (Long) -> Unit,
    onScrub: (Boolean) -> Unit,
    onFullscreen: () -> Unit,
    onPip: () -> Unit,
    onRotate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = state.durationMs.coerceAtLeast(0L)
    val span = duration.coerceAtLeast(1L)
    var scrubbing by remember { mutableStateOf(false) }
    var scrubMs by remember { mutableLongStateOf(0L) }
    val shownMs = if (scrubbing) scrubMs.coerceIn(0L, span) else state.positionMs.coerceIn(0L, span)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VodPlayerChrome.BottomScrim)
            .padding(
                start = VodPlayerChrome.BarHorizontal,
                end = VodPlayerChrome.BarHorizontal,
                top = 16.dp,
                bottom = VodPlayerChrome.BarVertical,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Util.timeMs(shownMs),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = VodPlayerChrome.MetaSize,
            )
            Slider(
                value = shownMs.toFloat(),
                onValueChange = { next ->
                    if (!scrubbing) onScrub(true)
                    scrubbing = true
                    scrubMs = next.toLong()
                },
                onValueChangeFinished = {
                    onSeek(scrubMs)
                    scrubbing = false
                    onScrub(false)
                },
                valueRange = 0f..span.toFloat(),
                enabled = duration > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = VodPlayerChrome.TrackActive,
                    inactiveTrackColor = VodPlayerChrome.TrackInactive,
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .height(28.dp),
            )
            Text(
                text = Util.timeMs(duration),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = VodPlayerChrome.MetaSize,
            )
            if (!state.fullscreen) {
                ControlIcon(R.drawable.ic_control_fullscreen, onFullscreen)
            }
            if (state.showPip) {
                ControlIcon(R.drawable.ic_control_pip, onPip)
            }
            if (state.showRotate) {
                ControlIcon(R.drawable.ic_control_rotate, onRotate)
            }
        }
    }
}

private fun controlHeadline(state: VodUiState): String {
    return buildString {
        append(state.title)
        if (state.episodeName.isNotEmpty()) {
            if (isNotEmpty()) append(' ')
            append(state.episodeName)
        }
    }
}

@Composable
private fun ControlIcon(
    icon: Int,
    onClick: () -> Unit,
    size: Dp = VodPlayerChrome.IconSize,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = Color.White,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(if (size >= VodPlayerChrome.PlayIconSize) 10.dp else 8.dp),
    )
}
