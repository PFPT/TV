package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import kotlinx.coroutines.launch

data class VodTimerState(
    val running: Boolean,
    val tickLabel: String,
)

enum class VodTimerContent {
    Choices,
    Countdown,
}

fun vodTimerContent(running: Boolean): VodTimerContent {
    return if (running) VodTimerContent.Countdown else VodTimerContent.Choices
}

object VodTimerIds {
    const val MINUTES = 1
    const val DELAY = 2
    const val CANCEL = 3
}

data class VodTimerEvent(
    val id: Int,
    val minutes: Int = 0,
)

fun interface VodTimerAction {
    fun onEvent(event: VodTimerEvent)
}

private val TimerSheetContainer = Color(0xFF1E1E1E)

private val TimerMinutes = listOf(
    5 to R.string.timer_5,
    15 to R.string.timer_15,
    30 to R.string.timer_30,
    60 to R.string.timer_60,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSheet(
    state: VodTimerState,
    onEvent: (VodTimerEvent) -> Unit,
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
        containerColor = TimerSheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.play_timer),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            when (vodTimerContent(state.running)) {
                VodTimerContent.Choices -> {
                    TimerMinutes.forEach { (minutes, label) ->
                        Text(
                            text = stringResource(label),
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEvent(VodTimerEvent(VodTimerIds.MINUTES, minutes))
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                        )
                    }
                }
                VodTimerContent.Countdown -> {
                    Text(
                        text = state.tickLabel,
                        color = Color.White,
                        fontSize = 48.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 24.dp),
                    )
                    TextButton(
                        onClick = { onEvent(VodTimerEvent(VodTimerIds.DELAY)) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(R.string.timer_delay), color = Color.White)
                    }
                    TextButton(
                        onClick = { onEvent(VodTimerEvent(VodTimerIds.CANCEL)) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(R.string.timer_cancel), color = Color.White)
                    }
                }
            }
        }
    }
}
