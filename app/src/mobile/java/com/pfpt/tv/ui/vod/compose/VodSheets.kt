package com.pfpt.tv.ui.vod.compose

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pfpt.tv.R
import com.pfpt.tv.playback.vod.session.VodEpisodeItem
import com.pfpt.tv.playback.vod.session.VodSessionViewModel
import com.pfpt.tv.ui.dialog.DownloadDialog
import com.pfpt.tv.utils.Util

private val SheetContainer = Color(0xFF1E1E1E)
private val SheetAccent = Color(0xFF8AB4F8)

enum class VodSheetKind {
    None,
    EpisodePlay,
    EpisodeDownload,
    DownloadConfirm,
    Info,
}

data class VodDownloadConfirm(
    val fileName: String,
    val url: String,
    val durationMs: Long,
    val headers: Map<String, String>,
)

data class VodInfoContent(
    val title: String,
    val url: String,
    val header: String,
    val singleLineTitle: Boolean,
)

fun interface VodDownloadSend {
    fun onSend(confirm: VodDownloadConfirm)
}

fun interface VodInfoShare {
    fun onShare(title: String)
}

class VodSheetController {
    var kind by mutableStateOf(VodSheetKind.None)
        private set
    var download by mutableStateOf<VodDownloadConfirm?>(null)
        private set
    var info by mutableStateOf<VodInfoContent?>(null)
        private set

    fun showEpisodes(download: Boolean) {
        this.download = null
        this.info = null
        kind = if (download) VodSheetKind.EpisodeDownload else VodSheetKind.EpisodePlay
    }

    fun showDownloadConfirm(fileName: String?, url: String?, durationMs: Long, headers: Map<String, String>?) {
        info = null
        download = VodDownloadConfirm(
            fileName = fileName.orEmpty(),
            url = url.orEmpty(),
            durationMs = durationMs,
            headers = headers?.toMap() ?: emptyMap(),
        )
        kind = VodSheetKind.DownloadConfirm
    }

    fun showInfo(title: CharSequence?, url: String?, headers: Map<String, String>?) {
        val formattedUrl = formatInfoUrl(url)
        val safeTitle = title?.toString().orEmpty()
        download = null
        info = VodInfoContent(
            title = safeTitle,
            url = formattedUrl,
            header = formatInfoHeader(headers),
            singleLineTitle = formattedUrl.isNotEmpty() && safeTitle.contains(formattedUrl),
        )
        kind = VodSheetKind.Info
    }

    fun dismiss() {
        kind = VodSheetKind.None
        download = null
        info = null
    }
}

private fun formatInfoUrl(url: String?): String {
    if (url.isNullOrEmpty()) return ""
    if (url.startsWith("data")) return url.substring(0, minOf(url.length, 128)) + "..."
    return url
}

private fun formatInfoHeader(headers: Map<String, String>?): String {
    if (headers.isNullOrEmpty()) return ""
    val sb = StringBuilder()
    for (key in headers.keys) sb.append(key).append(" : ").append(headers[key]).append('\n')
    return Util.substring(sb.toString()).orEmpty()
}

fun ComposeView.setVodSheetHost(
    session: VodSessionViewModel,
    controller: VodSheetController,
    onEpisodePlay: VodIndexClick,
    onEpisodeDownload: VodIndexClick,
    onDownloadList: Runnable,
    onDownloadConfirm: VodDownloadSend,
    onInfoShare: VodInfoShare,
) {
    setContent {
        val ui by session.uiState.collectAsStateWithLifecycle()
        val kind = controller.kind
        SideEffect {
            visibility = if (kind == VodSheetKind.None) View.GONE else View.VISIBLE
        }
        when (kind) {
            VodSheetKind.None -> Unit
            VodSheetKind.EpisodePlay -> EpisodePickerSheet(
                episodes = ui.episodes,
                download = false,
                onEpisode = { onEpisodePlay.onClick(it) },
                onOpenList = {},
                onDismiss = controller::dismiss,
            )
            VodSheetKind.EpisodeDownload -> EpisodePickerSheet(
                episodes = ui.episodes,
                download = true,
                onEpisode = { onEpisodeDownload.onClick(it) },
                onOpenList = {
                    onDownloadList.run()
                    controller.dismiss()
                },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.DownloadConfirm -> {
                val confirm = controller.download
                if (confirm != null) {
                    DownloadConfirmSheet(
                        confirm = confirm,
                        onConfirm = { onDownloadConfirm.onSend(confirm) },
                        onDismiss = controller::dismiss,
                    )
                }
            }
            VodSheetKind.Info -> {
                val info = controller.info
                if (info != null) {
                    InfoSheet(
                        info = info,
                        onShare = {
                            onInfoShare.onShare(info.title)
                            controller.dismiss()
                        },
                        onDismiss = controller::dismiss,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodePickerSheet(
    episodes: List<VodEpisodeItem>,
    download: Boolean,
    onEpisode: (Int) -> Unit,
    onOpenList: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()
    val selected = episodes.indexOfFirst { it.selected }
    LaunchedEffect(selected, episodes.size) {
        if (selected in episodes.indices) listState.scrollToItem(selected)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetContainer,
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(if (download) R.string.download_title else R.string.detail_episode),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            if (download) {
                Text(
                    text = stringResource(R.string.download_list),
                    color = SheetAccent,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onOpenList).padding(4.dp),
                )
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
        ) {
            itemsIndexed(episodes, key = { index, _ -> index }) { index, item ->
                Text(
                    text = item.name,
                    color = if (item.selected) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(
                            if (item.selected) Color.White else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(4.dp),
                        )
                        .clickable { onEpisode(index) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadConfirmSheet(
    confirm: VodDownloadConfirm,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val installed = DownloadDialog.isPlayerInstalled(LocalContext.current)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.download_file),
                color = Color.White,
                fontSize = 22.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.download_link),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { Util.copy(confirm.url) }) {
                    Text(text = stringResource(R.string.download_copy))
                }
            }
            Text(text = confirm.url, color = Color.White, fontSize = 14.sp)
            Text(
                text = stringResource(R.string.download_save_as),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = confirm.fileName,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = DownloadDialog.extension(confirm.url),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            if (confirm.durationMs > 0L) {
                Text(
                    text = stringResource(R.string.download_duration, Util.timeMs(confirm.durationMs)),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (!installed) {
                Text(
                    text = stringResource(R.string.download_missing),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.dialog_negative))
                }
                TextButton(onClick = onConfirm, enabled = installed) {
                    Text(text = stringResource(R.string.download_start))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSheet(
    info: VodInfoContent,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                text = info.title,
                color = Color.White,
                fontSize = 20.sp,
                maxLines = if (info.singleLineTitle) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.MiddleEllipsis,
            )
            if (info.header.isNotEmpty()) {
                Text(
                    text = info.header,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .combinedClickable(onClick = {}, onLongClick = {
                            Util.copy(info.header)
                        }),
                )
            }
            if (info.url.isNotEmpty()) {
                Text(
                    text = info.url,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .combinedClickable(onClick = onShare, onLongClick = {
                            Util.copy(info.url)
                        }),
                )
            }
        }
    }
}
