package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import com.pfpt.tv.utils.Util
import kotlinx.coroutines.launch

fun chapterRowLabel(label: String, timeUs: Long): String {
    if (timeUs == C.TIME_UNSET) return label
    return label + " [" + Util.timeMs(timeUs / 1000) + "]"
}

data class VodChapterState(
    val title: String,
    val names: List<String>,
    val selected: Int,
)

fun interface VodChapterAction {
    fun onIndex(index: Int)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSheet(
    state: VodChapterState,
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
