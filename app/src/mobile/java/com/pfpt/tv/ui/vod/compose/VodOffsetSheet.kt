package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

fun snapOffset(value: Float, from: Float, to: Float, step: Float): Float {
    val clamped = value.coerceIn(from, to)
    if (step <= 0f) return clamped
    val snapped = from + ((clamped - from) / step).roundToInt() * step
    return snapped.coerceIn(from, to)
}

fun offsetLabel(valueMs: Float): String {
    return String.format(Locale.US, "%+.1fs", valueMs / 1000f)
}

data class VodOffsetState(
    val title: String,
    val audio: Boolean,
    val valueMs: Float,
)

object VodOffsetIds {
    const val CHANGE = 1
    const val RESET = 2
}

data class VodOffsetEvent(
    val id: Int,
    val valueMs: Float = 0f,
)

fun interface VodOffsetAction {
    fun onEvent(event: VodOffsetEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffsetSheet(
    state: VodOffsetState,
    onEvent: (VodOffsetEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val from = if (state.audio) -10000f else -300000f
    val to = if (state.audio) 10000f else 300000f
    val step = if (state.audio) 100f else 1000f
    val value = snapOffset(state.valueMs, from, to, step)
    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = VodPickerSheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(VodOffsetEvent(VodOffsetIds.RESET)) }) {
                    Text(stringResource(R.string.danmaku_reset), color = Color.White)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(if (state.audio) R.string.offset_audio else R.string.offset_text),
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(text = offsetLabel(value), color = Color.White, fontSize = 14.sp)
            }
            Slider(
                value = value,
                onValueChange = { onEvent(VodOffsetEvent(VodOffsetIds.CHANGE, snapOffset(it, from, to, step))) },
                valueRange = from..to,
                steps = ((to - from) / step).roundToInt() - 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.24f),
                ),
            )
        }
    }
}
