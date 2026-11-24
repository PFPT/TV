package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import com.pfpt.tv.setting.PlayerSetting
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

data class EngineSelection(
    val exo: Boolean,
    val mpv: Boolean,
)

fun engineSelection(engine: Int): EngineSelection {
    return EngineSelection(
        exo = engine == PlayerSetting.ENGINE_EXO,
        mpv = engine == PlayerSetting.ENGINE_MPV,
    )
}

data class VodEngineState(
    val title: String,
    val exoSelected: Boolean,
    val mpvSelected: Boolean,
    val debugSelected: Boolean,
)

object VodEngineIds {
    const val EXO = 1
    const val MPV = 2
    const val DEBUG = 3
    const val OTHER = 4
}

data class VodEngineEvent(
    val id: Int,
)

fun interface VodEngineAction {
    fun onEvent(event: VodEngineEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSheet(
    state: VodEngineState,
    onEvent: (VodEngineEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(id: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onEvent(VodEngineEvent(id))
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
                IconButton(onClick = { leave(VodEngineIds.DEBUG) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_action_debug),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (state.debugSelected) Color.Black else Color.White),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (state.debugSelected) Color.White else Color.Transparent)
                            .padding(2.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                EngineChip(stringResource(R.string.play_exo), state.exoSelected) { leave(VodEngineIds.EXO) }
                EngineChip(stringResource(R.string.play_mpv), state.mpvSelected) { leave(VodEngineIds.MPV) }
                EngineChip(stringResource(R.string.play_other), false) { leave(VodEngineIds.OTHER) }
            }
        }
    }
}

@Composable
private fun EngineChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color.Black else Color.White,
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
