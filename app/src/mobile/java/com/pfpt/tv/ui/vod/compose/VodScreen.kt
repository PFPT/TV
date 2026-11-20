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
    onFlagClick: (Int) -> Unit = {},
    onEpisodeClick: (Int) -> Unit = {},
    onQualityClick: (Int) -> Unit = {},
    showCatalog: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        VodDetailHeader(
            state = state,
            onKeepClick = onKeepClick,
            onTitleClick = onTitleClick,
            onTitleLongClick = onTitleLongClick,
        )
        if (showCatalog) {
            VodCatalog(
                state = state,
                onFlagClick = onFlagClick,
                onEpisodeClick = onEpisodeClick,
                onQualityClick = onQualityClick,
            )
        }
    }
}

@Composable
fun VodCatalog(
    state: VodUiState,
    onFlagClick: (Int) -> Unit,
    onEpisodeClick: (Int) -> Unit,
    onQualityClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    VodFlagEpisodeSection(
        flags = state.flags,
        episodes = state.episodes,
        onFlagClick = onFlagClick,
        onEpisodeClick = onEpisodeClick,
        modifier = modifier,
        between = {
            VodQualityRow(
                qualities = state.qualities,
                qualityIndex = state.qualityIndex,
                onQualityClick = onQualityClick,
            )
        },
    )
}
