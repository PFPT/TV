package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import com.pfpt.tv.utils.Util
import kotlinx.coroutines.launch

fun editionRowLabel(label: String, durationUs: Long): String {
    if (durationUs == C.TIME_UNSET) return label
    return label + " [" + Util.timeMs(durationUs / 1000) + "]"
}

data class VodEditionState(
    val title: String,
    val names: List<String>,
    val selected: Int,
)

fun interface VodEditionAction {
    fun onIndex(index: Int)
}

internal val VodPickerSheetContainer = Color(0xFF1E1E1E)

@Composable
internal fun ConfigNameList(
    names: List<String>,
    selected: Int,
    onIndex: (Int) -> Unit,
) {
    if (names.isEmpty()) return
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        names.forEachIndexed { index, name ->
            val on = index == selected
            Text(
                text = name,
                color = if (on) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (on) Color.White else Color.White.copy(alpha = 0.12f))
                    .clickable { onIndex(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditionSheet(
    state: VodEditionState,
    onIndex: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(index: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onIndex(index)
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
            Text(
                text = state.title,
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            ConfigNameList(state.names, state.selected, ::leave)
        }
    }
}
