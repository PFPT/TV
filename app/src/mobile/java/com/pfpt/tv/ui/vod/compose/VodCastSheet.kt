package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import com.pfpt.tv.R
import kotlinx.coroutines.launch

data class VodCastState(
    val title: String,
    val names: List<String>,
    val hosts: List<String>,
)

object VodCastIds {
    const val DEVICE = 1
    const val SCAN = 2
    const val REFRESH = 3
}

data class VodCastEvent(
    val id: Int,
    val index: Int = -1,
)

fun interface VodCastAction {
    fun onEvent(event: VodCastEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CastSheet(
    state: VodCastState,
    onEvent: (VodCastEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
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
                IconButton(onClick = { onEvent(VodCastEvent(VodCastIds.SCAN)) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_action_scan),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = { onEvent(VodCastEvent(VodCastIds.REFRESH)) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_action_refresh),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            val count = minOf(state.names.size, state.hosts.size)
            if (count > 0) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    for (index in 0 until count) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(VodCastEvent(VodCastIds.DEVICE, index)) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = state.names[index],
                                color = Color.White,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = state.hosts[index],
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
