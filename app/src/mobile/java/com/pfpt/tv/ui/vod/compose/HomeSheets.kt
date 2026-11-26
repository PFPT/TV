package com.pfpt.tv.ui.vod.compose

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pfpt.tv.R
import kotlinx.coroutines.launch

data class HomeSiteInput(
    val name: String,
    val hidden: Boolean,
    val selected: Boolean,
    val changeable: Boolean,
)

data class HomeSiteRow(
    val name: String,
    val selected: Boolean,
    val changeable: Boolean,
)

fun homeSiteRows(sites: List<HomeSiteInput>): List<HomeSiteRow> {
    return sites.filter { !it.hidden }.map { HomeSiteRow(it.name, it.selected, it.changeable) }
}

fun homeHistoryNames(names: List<String>, readOnly: Boolean): List<String> {
    if (names.isEmpty() || readOnly) return names
    return names.drop(1)
}

fun homeLinkReady(text: String): Boolean {
    return text.trim().isNotEmpty()
}

fun firstSelected(selected: List<Boolean>): Int {
    return selected.indexOfFirst { it }
}

enum class HomeSheetKind {
    None,
    Site,
    Filter,
    History,
    Link,
    Receive,
}

data class HomeFilterGroup(
    val labels: List<String>,
    val selected: Int,
)

data class HomeSiteEvent(val id: Int, val index: Int)

object HomeSiteIds {
    const val OPEN = 1
    const val CHANGE = 2
    const val CHANGE_ALL = 3
}

data class HomeFilterEvent(val group: Int, val index: Int)

data class HomeLinkEvent(val id: Int, val text: String = "")

object HomeLinkIds {
    const val PLAY = 1
    const val CHOOSE = 2
}

class HomeSheetController {
    var kind by mutableStateOf(HomeSheetKind.None)
        private set
    var sites by mutableStateOf(emptyList<HomeSiteRow>())
        private set
    var filters by mutableStateOf(emptyList<HomeFilterGroup>())
        private set
    var history by mutableStateOf(emptyList<String>())
        private set
    var link by mutableStateOf("")
        private set
    var receive by mutableStateOf(VodReceiveState(name = "", from = "", pic = "", loading = false))
        private set

    fun showSite(rows: List<HomeSiteRow>) {
        sites = rows
        kind = HomeSheetKind.Site
    }

    fun updateSite(rows: List<HomeSiteRow>) {
        if (kind != HomeSheetKind.Site) return
        sites = rows
    }

    fun showFilter(groups: List<HomeFilterGroup>) {
        filters = groups
        kind = HomeSheetKind.Filter
    }

    fun updateFilter(groups: List<HomeFilterGroup>) {
        if (kind != HomeSheetKind.Filter) return
        filters = groups
    }

    fun showHistory(names: List<String>) {
        history = names
        kind = HomeSheetKind.History
    }

    fun showLink(text: String) {
        link = text
        kind = HomeSheetKind.Link
    }

    fun showReceive(state: VodReceiveState) {
        receive = state
        kind = HomeSheetKind.Receive
    }

    fun updateReceive(state: VodReceiveState) {
        if (kind != HomeSheetKind.Receive) return
        receive = state
    }

    fun dismiss() {
        kind = HomeSheetKind.None
    }
}

fun interface HomeSiteAction {
    fun onEvent(event: HomeSiteEvent)
}

fun interface HomeFilterAction {
    fun onEvent(event: HomeFilterEvent)
}

fun interface HomeHistoryAction {
    fun onIndex(index: Int)
}

fun interface HomeLinkAction {
    fun onEvent(event: HomeLinkEvent)
}

fun ComposeView.setHomeSheetHost(
    controller: HomeSheetController,
    onSite: HomeSiteAction,
    onFilter: HomeFilterAction,
    onHistory: HomeHistoryAction,
    onLink: HomeLinkAction,
    onReceive: VodReceiveAction,
    onReceiveDismiss: Runnable,
) {
    setContent {
        val kind = controller.kind
        LaunchedEffect(kind) {
            visibility = if (kind == HomeSheetKind.None) View.GONE else View.VISIBLE
        }
        when (kind) {
            HomeSheetKind.None -> Unit
            HomeSheetKind.Site -> HomeSiteSheet(controller.sites, { onSite.onEvent(it) }, controller::dismiss)
            HomeSheetKind.Filter -> HomeFilterSheet(controller.filters, { onFilter.onEvent(it) }, controller::dismiss)
            HomeSheetKind.History -> HomeHistorySheet(controller.history, { onHistory.onIndex(it) }, controller::dismiss)
            HomeSheetKind.Link -> HomeLinkSheet(controller.link, { onLink.onEvent(it) }, controller::dismiss)
            HomeSheetKind.Receive -> ReceiveSheet(controller.receive, { onReceive.onPlay() }, onReceiveDismiss::run)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSiteSheet(rows: List<HomeSiteRow>, onEvent: (HomeSiteEvent) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(index: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onEvent(HomeSiteEvent(HomeSiteIds.OPEN, index))
        }
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
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.name,
                        color = if (row.selected) Color.Black else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (row.selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (row.selected) Color.White else Color.White.copy(alpha = 0.12f))
                            .clickable { leave(index) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    Image(
                        painter = painterResource(if (row.changeable) R.drawable.ic_site_change else R.drawable.ic_site_block),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)
                            .combinedClickable(
                                onClick = { onEvent(HomeSiteEvent(HomeSiteIds.CHANGE, index)) },
                                onLongClick = { onEvent(HomeSiteEvent(HomeSiteIds.CHANGE_ALL, index)) },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFilterSheet(groups: List<HomeFilterGroup>, onEvent: (HomeFilterEvent) -> Unit, onDismiss: () -> Unit) {
    HomeSheet(onDismiss) {
        groups.forEachIndexed { group, item ->
            VodChipRow(item.labels, item.selected, onClick = { index ->
                onEvent(HomeFilterEvent(group, index))
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHistorySheet(names: List<String>, onIndex: (Int) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    fun leave(index: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onIndex(index)
        }
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
            ConfigNameList(names, -1) { leave(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeLinkSheet(initial: String, onEvent: (HomeLinkEvent) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var text by remember(initial) { mutableStateOf(initial) }
    fun leave() {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }
    ModalBottomSheet(
        onDismissRequest = { leave() },
        sheetState = sheetState,
        containerColor = VodPickerSheetContainer,
        contentColor = Color.White,
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(
                text = stringResource(R.string.play),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.dialog_config_url), color = Color.White.copy(alpha = 0.5f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onEvent(HomeLinkEvent(HomeLinkIds.PLAY, text)) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                    ),
                )
                IconButton(onClick = { onEvent(HomeLinkEvent(HomeLinkIds.CHOOSE)) }) {
                    Image(
                        painter = painterResource(R.drawable.ic_action_choose),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { leave() }) {
                    Text(stringResource(R.string.dialog_negative), color = Color.White)
                }
                TextButton(onClick = { onEvent(HomeLinkEvent(HomeLinkIds.PLAY, text)) }) {
                    Text(stringResource(R.string.dialog_positive), color = Color.White)
                }
            }
        }
    }
}
