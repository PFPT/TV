package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pfpt.tv.playback.vod.session.VodUiState

@Composable
fun VodScreen(
    state: VodUiState,
    onKeepClick: () -> Unit,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onSiteClick: () -> Unit = {},
    onRemarkClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        VodDetailHeader(
            state = state,
            onKeepClick = onKeepClick,
            onTitleClick = onTitleClick,
            onTitleLongClick = onTitleLongClick,
            onSiteClick = onSiteClick,
            onRemarkClick = onRemarkClick,
        )
    }
}
