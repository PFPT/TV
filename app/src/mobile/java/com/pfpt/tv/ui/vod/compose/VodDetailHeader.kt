package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.playback.vod.session.VodUiState

@Composable
fun VodDetailHeader(
    state: VodUiState,
    onKeepClick: () -> Unit,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text(
            text = state.title,
            color = Color.White,
            fontSize = 20.sp,
            maxLines = 3,
            modifier = Modifier.combinedClickable(onClick = onTitleClick, onLongClick = onTitleLongClick),
        )
        if (state.remark.isNotEmpty()) {
            Text(text = state.remark, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        if (state.siteName.isNotEmpty()) {
            Text(text = state.siteName, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
        TextButton(onClick = onKeepClick) {
            Text(text = if (state.kept) "已收藏" else "收藏", color = Color.White)
        }
    }
}
