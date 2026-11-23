package com.pfpt.tv.ui.vod.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VodQualityRow(
    qualities: List<String>,
    qualityIndex: Int,
    onQualityClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    VodChipRow(
        labels = qualities,
        selectedIndex = qualityIndex,
        onClick = onQualityClick,
        modifier = modifier,
    )
}
