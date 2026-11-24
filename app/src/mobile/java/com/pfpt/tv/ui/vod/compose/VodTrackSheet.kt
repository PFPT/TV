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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import com.pfpt.tv.R
import kotlinx.coroutines.launch

data class VodTrackActions(
    val showChoose: Boolean,
    val showSubtitle: Boolean,
    val showOffset: Boolean,
)

fun vodTrackActions(type: Int, isVod: Boolean, haveTrack: Boolean): VodTrackActions {
    val text = type == C.TRACK_TYPE_TEXT
    val audio = type == C.TRACK_TYPE_AUDIO
    return VodTrackActions(
        showChoose = text && isVod,
        showSubtitle = text && haveTrack,
        showOffset = (text || audio) && haveTrack,
    )
}

data class VodTrackState(
    val title: String,
    val names: List<String>,
    val selected: List<Int>,
    val showChoose: Boolean,
    val showSubtitle: Boolean,
    val showOffset: Boolean,
)

object VodTrackIds {
    const val TRACK = 1
    const val CHOOSE = 2
    const val SUBTITLE = 3
    const val OFFSET = 4
}

data class VodTrackEvent(
    val id: Int,
    val index: Int = -1,
)

fun interface VodTrackAction {
    fun onEvent(event: VodTrackEvent)
}

private val TrackSheetContainer = Color(0xFF1E1E1E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackSheet(
    state: VodTrackState,
    onEvent: (VodTrackEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(event: VodTrackEvent) {
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
        containerColor = TrackSheetContainer,
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
                if (state.showChoose) {
                    IconButton(onClick = { onEvent(VodTrackEvent(VodTrackIds.CHOOSE)) }) {
                        Image(
                            painter = painterResource(R.drawable.ic_action_choose),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                if (state.showSubtitle) {
                    IconButton(onClick = { leave(VodTrackEvent(VodTrackIds.SUBTITLE)) }) {
                        Image(
                            painter = painterResource(R.drawable.ic_action_subtitle),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                if (state.showOffset) {
                    IconButton(onClick = { leave(VodTrackEvent(VodTrackIds.OFFSET)) }) {
                        Image(
                            painter = painterResource(R.drawable.ic_action_offset),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            if (state.names.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.names.forEachIndexed { index, name ->
                        val selected = index in state.selected
                        Text(
                            text = name,
                            color = if (selected) Color.Black else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) Color.White else Color.White.copy(alpha = 0.12f))
                                .clickable { leave(VodTrackEvent(VodTrackIds.TRACK, index)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
