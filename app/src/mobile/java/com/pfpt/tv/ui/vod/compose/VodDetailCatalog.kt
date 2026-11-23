package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodUiState

@Composable
fun VodDetailCatalog(
    state: VodUiState,
    onFlagClick: (Int) -> Unit,
    onQualityClick: (Int) -> Unit,
    onEpisodeClick: (Int) -> Unit,
    onQuickClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (state.flags.isNotEmpty()) {
            VodSectionTitle(stringResource(R.string.detail_flag))
            VodChipRow(
                labels = state.flags.map { it.name },
                selectedIndex = state.flags.indexOfFirst { it.selected },
                onClick = onFlagClick,
            )
        }
        if (state.qualities.isNotEmpty()) {
            VodSectionTitle(stringResource(R.string.detail_quality))
            VodQualityRow(
                qualities = state.qualities,
                qualityIndex = state.qualityIndex,
                onQualityClick = onQualityClick,
            )
        }
        if (state.episodes.isNotEmpty()) {
            VodSectionTitle(stringResource(R.string.detail_episode))
            VodChipRow(
                labels = state.episodes.map { it.name },
                selectedIndex = state.episodes.indexOfFirst { it.selected },
                onClick = onEpisodeClick,
            )
        }
        VodQuickGrid(items = state.quick, onClick = onQuickClick)
    }
}

@Composable
private fun VodSectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
    )
}