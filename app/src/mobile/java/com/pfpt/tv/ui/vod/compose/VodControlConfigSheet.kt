package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class VodControlConfig(
    val speed: Float,
    val showParse: Boolean,
    val parseNames: List<String>,
    val parseIndex: Int,
    val scaleLabels: List<String>,
    val scaleIndex: Int,
    val playerLabel: String,
    val decodeLabel: String,
    val openingLabel: String,
    val endingLabel: String,
    val showEdition: Boolean,
    val showChapter: Boolean,
    val showText: Boolean,
    val showAudio: Boolean,
    val showVideo: Boolean,
    val showTrackTitle: Boolean,
    val showDanmaku: Boolean,
    val repeatOn: Boolean,
    val timerOn: Boolean,
)

object VodControlConfigIds {
    const val SPEED = 1
    const val PARSE = 2
    const val SCALE = 3
    const val PLAYER = 4
    const val DECODE = 5
    const val OPENING = 6
    const val OPENING_RESET = 7
    const val ENDING = 8
    const val ENDING_RESET = 9
    const val EDITION = 10
    const val CHAPTER = 11
    const val TEXT = 12
    const val AUDIO = 13
    const val VIDEO = 14
    const val DANMAKU = 15
    const val REPEAT = 16
    const val TIMER = 17
}

data class VodControlConfigEvent(
    val id: Int,
    val index: Int = -1,
    val speed: Float = 0f,
)

fun sanitizeVodControlConfig(raw: VodControlConfig): VodControlConfig {
    val parseNames = if (raw.showParse) raw.parseNames else emptyList()
    val parseIndex = if (raw.showParse && raw.parseIndex in parseNames.indices) raw.parseIndex else -1
    val scaleIndex = if (raw.scaleIndex in raw.scaleLabels.indices) raw.scaleIndex else -1
    return raw.copy(
        speed = sanitizeSpeed(raw.speed),
        parseNames = parseNames,
        parseIndex = parseIndex,
        scaleIndex = scaleIndex,
        showTrackTitle = raw.showText || raw.showAudio || raw.showVideo,
    )
}

private fun sanitizeSpeed(speed: Float): Float {
    if (!speed.isFinite() || speed < 0.5f || speed > 5f) return 0.5f
    val steps = ((speed - 0.5f) / 0.25f).roundToInt()
    return 0.5f + steps * 0.25f
}

private val ConfigSheetContainer = Color(0xFF1E1E1E)

fun interface VodControlConfigAction {
    fun onEvent(event: VodControlConfigEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlConfigSheet(
    config: VodControlConfig,
    onEvent: (VodControlConfigEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(id: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onEvent(VodControlConfigEvent(id))
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = ConfigSheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            ConfigSectionTitle(stringResource(R.string.control_speed))
            Slider(
                value = config.speed,
                onValueChange = { speed ->
                    onEvent(VodControlConfigEvent(VodControlConfigIds.SPEED, speed = speed))
                },
                valueRange = 0.5f..5f,
                steps = 17,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            if (config.showParse) {
                ConfigSectionTitle(stringResource(R.string.parse))
                VodChipRow(
                    labels = config.parseNames,
                    selectedIndex = config.parseIndex,
                    onClick = { index ->
                        onEvent(VodControlConfigEvent(VodControlConfigIds.PARSE, index = index))
                    },
                )
            }
            ConfigSectionTitle(stringResource(R.string.control_scale))
            VodChipRow(
                labels = config.scaleLabels,
                selectedIndex = config.scaleIndex,
                onClick = { index ->
                    onEvent(VodControlConfigEvent(VodControlConfigIds.SCALE, index = index))
                },
            )
            ConfigSectionTitle(stringResource(R.string.play))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ConfigActionChip(config.playerLabel, false, { leave(VodControlConfigIds.PLAYER) })
                ConfigActionChip(config.decodeLabel, false, {
                    onEvent(VodControlConfigEvent(VodControlConfigIds.DECODE))
                })
                ConfigActionChip(
                    label = config.openingLabel,
                    selected = false,
                    onClick = { onEvent(VodControlConfigEvent(VodControlConfigIds.OPENING)) },
                    onLongClick = { onEvent(VodControlConfigEvent(VodControlConfigIds.OPENING_RESET)) },
                )
                ConfigActionChip(
                    label = config.endingLabel,
                    selected = false,
                    onClick = { onEvent(VodControlConfigEvent(VodControlConfigIds.ENDING)) },
                    onLongClick = { onEvent(VodControlConfigEvent(VodControlConfigIds.ENDING_RESET)) },
                )
                if (config.showEdition) {
                    ConfigActionChip(stringResource(R.string.play_edition), false, { leave(VodControlConfigIds.EDITION) })
                }
                if (config.showChapter) {
                    ConfigActionChip(stringResource(R.string.play_chapter), false, { leave(VodControlConfigIds.CHAPTER) })
                }
            }
            if (config.showTrackTitle) {
                ConfigSectionTitle(stringResource(R.string.control_track))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (config.showText) {
                        ConfigActionChip(stringResource(R.string.play_track_text), false, { leave(VodControlConfigIds.TEXT) })
                    }
                    if (config.showAudio) {
                        ConfigActionChip(stringResource(R.string.play_track_audio), false, { leave(VodControlConfigIds.AUDIO) })
                    }
                    if (config.showVideo) {
                        ConfigActionChip(stringResource(R.string.play_track_video), false, { leave(VodControlConfigIds.VIDEO) })
                    }
                }
            }
            ConfigSectionTitle(stringResource(R.string.control_other))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (config.showDanmaku) {
                    ConfigActionChip(stringResource(R.string.danmaku), false, { leave(VodControlConfigIds.DANMAKU) })
                }
                ConfigActionChip(stringResource(R.string.play_repeat), config.repeatOn, {
                    onEvent(VodControlConfigEvent(VodControlConfigIds.REPEAT))
                })
                ConfigActionChip(stringResource(R.string.play_timer), config.timerOn, {
                    leave(VodControlConfigIds.TIMER)
                })
            }
        }
    }
}

@Composable
private fun ConfigSectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun ConfigActionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val gesture = if (onLongClick != null) {
        Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Text(
        text = label,
        color = if (selected) Color.Black else Color.White,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.12f))
            .then(gesture)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}