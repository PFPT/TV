package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pfpt.tv.R
import kotlinx.coroutines.launch

object VodSubtitleIds {
    const val LARGE = 1
    const val SMALL = 2
    const val UP = 3
    const val DOWN = 4
    const val RESET = 5
}

fun interface VodSubtitleAction {
    fun onAction(id: Int)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitleSheet(
    onAction: (Int) -> Unit,
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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            SubtitleButton(R.drawable.ic_subtitle_large) { onAction(VodSubtitleIds.LARGE) }
            SubtitleButton(R.drawable.ic_subtitle_small) { onAction(VodSubtitleIds.SMALL) }
            SubtitleButton(R.drawable.ic_subtitle_up) { onAction(VodSubtitleIds.UP) }
            SubtitleButton(R.drawable.ic_subtitle_down) { onAction(VodSubtitleIds.DOWN) }
            SubtitleButton(R.drawable.ic_subtitle_reset) { onAction(VodSubtitleIds.RESET) }
        }
    }
}

@Composable
private fun SubtitleButton(icon: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(24.dp),
        )
    }
}
