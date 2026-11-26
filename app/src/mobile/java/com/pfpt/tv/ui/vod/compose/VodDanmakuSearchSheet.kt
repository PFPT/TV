package com.pfpt.tv.ui.vod.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pfpt.tv.R
import kotlinx.coroutines.launch

fun danmakuSearchReady(keyword: String): Boolean {
    return keyword.trim().isNotEmpty()
}

data class VodDanmakuSearchState(
    val keyword: String,
    val loading: Boolean,
    val names: List<String>,
    val selected: Int,
)

object VodDanmakuSearchIds {
    const val SEARCH = 1
    const val ITEM = 2
}

data class VodDanmakuSearchEvent(
    val id: Int,
    val index: Int = -1,
    val keyword: String = "",
)

fun interface VodDanmakuSearchAction {
    fun onEvent(event: VodDanmakuSearchEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanmakuSearchSheet(
    state: VodDanmakuSearchState,
    onEvent: (VodDanmakuSearchEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var text by remember(state.keyword) { mutableStateOf(state.keyword) }
    fun leave(event: VodDanmakuSearchEvent) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onEvent(event)
        }
    }
    fun search() {
        if (!danmakuSearchReady(text)) return
        keyboard?.hide()
        onEvent(VodDanmakuSearchEvent(VodDanmakuSearchIds.SEARCH, keyword = text))
    }
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 255) text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .focusRequester(focus),
                singleLine = true,
                placeholder = { androidx.compose.material3.Text(stringResource(R.string.search_keyword), color = Color.White.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = { search() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                ),
            )
            if (state.loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp),
                )
            } else {
                ConfigNameList(state.names, state.selected) { index ->
                    leave(VodDanmakuSearchEvent(VodDanmakuSearchIds.ITEM, index))
                }
            }
        }
    }
}
