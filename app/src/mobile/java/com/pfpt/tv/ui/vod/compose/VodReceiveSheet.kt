package com.pfpt.tv.ui.vod.compose

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.R as Media3UiR
import com.pfpt.tv.utils.ImgUtil
import kotlinx.coroutines.launch

data class VodReceiveState(
    val name: String,
    val from: String,
    val pic: String,
    val loading: Boolean,
)

fun interface VodReceiveAction {
    fun onPlay()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveSheet(
    state: VodReceiveState,
    onPlay: () -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 120.dp)
                    .clickable(enabled = !state.loading, onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                AndroidView(
                    factory = { context -> ImageView(context) },
                    update = { view -> ImgUtil.load(state.name, state.pic, view) },
                    modifier = Modifier.matchParentSize(),
                )
                if (state.loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                } else {
                    Icon(
                        painter = painterResource(Media3UiR.drawable.exo_icon_play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = state.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.from,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
