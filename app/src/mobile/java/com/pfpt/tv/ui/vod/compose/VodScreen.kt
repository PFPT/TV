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
    onDownloadClick: () -> Unit = {},
    onReverseClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onCopyContent: () -> Unit = {},
    onFlagClick: (Int) -> Unit = {},
    onQualityClick: (Int) -> Unit = {},
    onEpisodeClick: (Int) -> Unit = {},
    onQuickClick: (Int) -> Unit = {},
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
            onDownloadClick = onDownloadClick,
            onReverseClick = onReverseClick,
            onMoreClick = onMoreClick,
            onCopyContent = onCopyContent,
        )
        VodDetailCatalog(
            state = state,
            onFlagClick = onFlagClick,
            onQualityClick = onQualityClick,
            onEpisodeClick = onEpisodeClick,
            onQuickClick = onQuickClick,
        )
    }
}