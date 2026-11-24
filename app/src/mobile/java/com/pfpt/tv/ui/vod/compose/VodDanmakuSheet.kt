package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import kotlinx.coroutines.launch

fun danmakuShowSearch(hasMetadata: Boolean, apiUrl: String?): Boolean {
    return hasMetadata && !apiUrl.isNullOrEmpty()
}

data class VodDanmakuState(
    val title: String,
    val names: List<String>,
    val selected: Int,
    val showSearch: Boolean,
)

object VodDanmakuIds {
    const val ITEM = 1
    const val SEARCH = 2
    const val CHOOSE = 3
    const val SETTING = 4
}

data class VodDanmakuEvent(
    val id: Int,
    val index: Int = -1,
)

fun interface VodDanmakuAction {
    fun onEvent(event: VodDanmakuEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanmakuSheet(
    state: VodDanmakuState,
    onEvent: (VodDanmakuEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(event: VodDanmakuEvent) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onEvent(event)
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
                if (state.showSearch) {
                    IconButton(onClick = { leave(VodDanmakuEvent(VodDanmakuIds.SEARCH)) }) {
                        Image(
                            painter = painterResource(R.drawable.ic_action_search),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                IconButton(onClick = { onEvent(VodDanmakuEvent(VodDanmakuIds.CHOOSE)) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_action_choose),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = { leave(VodDanmakuEvent(VodDanmakuIds.SETTING)) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_danmaku_setting),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            ConfigNameList(state.names, state.selected) { index ->
                leave(VodDanmakuEvent(VodDanmakuIds.ITEM, index))
            }
        }
    }
}
