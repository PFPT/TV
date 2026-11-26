package com.pfpt.tv.ui.vod.compose

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.SheetState
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
import kotlinx.coroutines.launch

private val SheetContainer = Color(0xFF1E1E1E)
private val SheetAccent = Color(0xFF8AB4F8)
private val EpisodeSelectedBg = Color.White
private val EpisodeUnselectedBg = Color.White.copy(alpha = 0.08f)
private const val SheetMotionMs = 200
private const val StaggerDelayMs = 25
private const val StaggerMaxIndex = 7

enum class VodSheetKind {
    None,
    EpisodePlay,
    EpisodeDownload,
    DownloadConfirm,
    Info,
    ControlMore,
    ControlConfig,
    Timer,
    Track,
    Edition,
    Chapter,
    Engine,
    Danmaku,
    Subtitle,
    Offset,
    Cast,
    Receive,
    DanmakuSearch,
    DanmakuSetting,
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

fun interface VodControlMoreAction {
    fun onAction(id: Int)
}

object VodControlMoreIds {
    const val TEXT = 1
    const val AUDIO = 2
    const val VIDEO = 3
    const val DANMAKU = 4
    const val REPLAY = 5
    const val RESET = 6
    const val ALL_SETTINGS = 7
    const val INFO = 8
    const val EPISODES = 9
    const val SPEED = 10
    const val SCALE = 11
    const val DECODE = 12
    const val REPEAT = 13
    const val OPENING = 14
    const val ENDING = 15
    const val SUBTITLE = 16
}

data class VodControlMoreVisibility(
    val showText: Boolean = true,
    val showAudio: Boolean = false,
    val showVideo: Boolean = false,
    val showDanmaku: Boolean = false,
    val showSpeed: Boolean = true,
    val speedLabel: String = "",
    val scaleLabel: String = "",
    val decodeLabel: String = "",
    val openingLabel: String = "",
    val endingLabel: String = "",
    val repeatOn: Boolean = false,
)

class VodSheetController {
    var kind by mutableStateOf(VodSheetKind.None)
        private set
    var download by mutableStateOf<VodDownloadConfirm?>(null)
        private set
    var info by mutableStateOf<VodInfoContent?>(null)
        private set
    var controlMore by mutableStateOf(VodControlMoreVisibility())
        private set
    var controlConfig by mutableStateOf(
        VodControlConfig(
            speed = 0.5f,
            showParse = false,
            parseNames = emptyList(),
            parseIndex = -1,
            scaleLabels = emptyList(),
            scaleIndex = -1,
            playerLabel = "",
            decodeLabel = "",
            openingLabel = "",
            endingLabel = "",
            showEdition = false,
            showChapter = false,
            showText = false,
            showAudio = false,
            showVideo = false,
            showTrackTitle = false,
            showDanmaku = false,
            repeatOn = false,
            timerOn = false,
        ),
    )
        private set
    var timer by mutableStateOf(VodTimerState(running = false, tickLabel = ""))
        private set
    var track by mutableStateOf(
        VodTrackState(
            title = "",
            names = emptyList(),
            selected = emptyList(),
            showChoose = false,
            showSubtitle = false,
            showOffset = false,
        ),
    )
        private set
    var edition by mutableStateOf(VodEditionState(title = "", names = emptyList(), selected = -1))
        private set
    var chapter by mutableStateOf(VodChapterState(title = "", names = emptyList(), selected = -1))
        private set
    var engine by mutableStateOf(VodEngineState(title = "", exoSelected = false, mpvSelected = false, debugSelected = false))
        private set
    var danmaku by mutableStateOf(VodDanmakuState(title = "", names = emptyList(), selected = -1, showSearch = false))
        private set
    var offset by mutableStateOf(VodOffsetState(title = "", audio = false, valueMs = 0f))
        private set
    var cast by mutableStateOf(VodCastState(title = "", names = emptyList(), hosts = emptyList()))
        private set
    var receive by mutableStateOf(VodReceiveState(name = "", from = "", pic = "", loading = false))
        private set
    var danmakuSearch by mutableStateOf(VodDanmakuSearchState(keyword = "", loading = false, names = emptyList(), selected = -1))
        private set
    var danmakuSetting by mutableStateOf(blankDanmakuSetting())
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

    fun showControlMore(visibility: VodControlMoreVisibility = VodControlMoreVisibility()) {
        download = null
        info = null
        controlMore = visibility
        kind = VodSheetKind.ControlMore
    }

    fun showControlConfig(config: VodControlConfig) {
        download = null
        info = null
        controlConfig = config
        kind = VodSheetKind.ControlConfig
    }

    fun updateControlConfig(config: VodControlConfig) {
        if (kind != VodSheetKind.ControlConfig) return
        controlConfig = config
    }

    fun showTimer(state: VodTimerState) {
        download = null
        info = null
        timer = state
        kind = VodSheetKind.Timer
    }

    fun updateTimer(state: VodTimerState) {
        if (kind != VodSheetKind.Timer) return
        timer = state
    }

    fun showTrack(state: VodTrackState) {
        download = null
        info = null
        track = state
        kind = VodSheetKind.Track
    }

    fun showEdition(state: VodEditionState) {
        download = null
        info = null
        edition = state
        kind = VodSheetKind.Edition
    }

    fun showChapter(state: VodChapterState) {
        download = null
        info = null
        chapter = state
        kind = VodSheetKind.Chapter
    }

    fun showEngine(state: VodEngineState) {
        download = null
        info = null
        engine = state
        kind = VodSheetKind.Engine
    }

    fun showDanmaku(state: VodDanmakuState) {
        download = null
        info = null
        danmaku = state
        kind = VodSheetKind.Danmaku
    }

    fun showSubtitle() {
        download = null
        info = null
        kind = VodSheetKind.Subtitle
    }

    fun showOffset(state: VodOffsetState) {
        download = null
        info = null
        offset = state
        kind = VodSheetKind.Offset
    }

    fun updateOffset(state: VodOffsetState) {
        if (kind != VodSheetKind.Offset) return
        offset = state
    }

    fun showCast(state: VodCastState) {
        download = null
        info = null
        cast = state
        kind = VodSheetKind.Cast
    }

    fun updateCast(state: VodCastState) {
        if (kind != VodSheetKind.Cast) return
        cast = state
    }

    fun showReceive(state: VodReceiveState) {
        download = null
        info = null
        receive = state
        kind = VodSheetKind.Receive
    }

    fun updateReceive(state: VodReceiveState) {
        if (kind != VodSheetKind.Receive) return
        receive = state
    }

    fun showDanmakuSearch(state: VodDanmakuSearchState) {
        download = null
        info = null
        danmakuSearch = state
        kind = VodSheetKind.DanmakuSearch
    }

    fun updateDanmakuSearch(state: VodDanmakuSearchState) {
        if (kind != VodSheetKind.DanmakuSearch) return
        danmakuSearch = state
    }

    fun showDanmakuSetting(state: VodDanmakuSettingState) {
        download = null
        info = null
        danmakuSetting = state
        kind = VodSheetKind.DanmakuSetting
    }

    fun updateDanmakuSetting(state: VodDanmakuSettingState) {
        if (kind != VodSheetKind.DanmakuSetting) return
        danmakuSetting = state
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
    onControlMore: VodControlMoreAction,
    onControlConfig: VodControlConfigAction,
    onTimer: VodTimerAction,
    onTimerDismiss: Runnable,
    onTrack: VodTrackAction,
    onEdition: VodEditionAction,
    onChapter: VodChapterAction,
    onEngine: VodEngineAction,
    onDanmaku: VodDanmakuAction,
    onSubtitle: VodSubtitleAction,
    onOffset: VodOffsetAction,
    onCast: VodCastAction,
    onCastDismiss: Runnable,
    onReceive: VodReceiveAction,
    onReceiveDismiss: Runnable,
    onDanmakuSearch: VodDanmakuSearchAction,
    onDanmakuSearchDismiss: Runnable,
    onDanmakuSetting: VodDanmakuSettingAction,
) {
    setContent {
        val ui by session.uiState.collectAsStateWithLifecycle()
        val kind = controller.kind
        LaunchedEffect(kind) {
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
                onOpenList = onDownloadList::run,
                onDismiss = controller::dismiss,
            )
            VodSheetKind.DownloadConfirm -> {
                val ready = ui.downloadReady
                val confirm = controller.download ?: ready?.let {
                    VodDownloadConfirm(
                        fileName = it.fileName,
                        url = it.url,
                        durationMs = it.durationMs,
                        headers = it.headers,
                    )
                }
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
                        onShare = { onInfoShare.onShare(info.title) },
                        onDismiss = controller::dismiss,
                    )
                }
            }
            VodSheetKind.ControlMore -> ControlMoreSheet(
                visibility = controller.controlMore,
                onAction = { id ->
                    onControlMore.onAction(id)
                },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.ControlConfig -> ControlConfigSheet(
                config = controller.controlConfig,
                onEvent = { onControlConfig.onEvent(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Timer -> TimerSheet(
                state = controller.timer,
                onEvent = { onTimer.onEvent(it) },
                onDismiss = onTimerDismiss::run,
            )
            VodSheetKind.Track -> TrackSheet(
                state = controller.track,
                onEvent = { onTrack.onEvent(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Edition -> EditionSheet(
                state = controller.edition,
                onIndex = { onEdition.onIndex(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Chapter -> ChapterSheet(
                state = controller.chapter,
                onIndex = { onChapter.onIndex(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Engine -> EngineSheet(
                state = controller.engine,
                onEvent = { onEngine.onEvent(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Danmaku -> DanmakuSheet(
                state = controller.danmaku,
                onEvent = { onDanmaku.onEvent(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Subtitle -> SubtitleSheet(
                onAction = { onSubtitle.onAction(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Offset -> OffsetSheet(
                state = controller.offset,
                onEvent = { onOffset.onEvent(it) },
                onDismiss = controller::dismiss,
            )
            VodSheetKind.Cast -> CastSheet(
                state = controller.cast,
                onEvent = { onCast.onEvent(it) },
                onDismiss = onCastDismiss::run,
            )
            VodSheetKind.Receive -> ReceiveSheet(
                state = controller.receive,
                onPlay = { onReceive.onPlay() },
                onDismiss = onReceiveDismiss::run,
            )
            VodSheetKind.DanmakuSearch -> DanmakuSearchSheet(
                state = controller.danmakuSearch,
                onEvent = { onDanmakuSearch.onEvent(it) },
                onDismiss = onDanmakuSearchDismiss::run,
            )
            VodSheetKind.DanmakuSetting -> DanmakuSettingSheet(
                state = controller.danmakuSetting,
                onEvent = { onDanmakuSetting.onEvent(it) },
                onDismiss = controller::dismiss,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberHideThenDismiss(
    sheetState: SheetState,
    onDismiss: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    return remember(sheetState, onDismiss) {
        {
            scope.launch {
                sheetState.hide()
                onDismiss()
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
    val dismiss = rememberHideThenDismiss(sheetState, onDismiss)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val selected = episodes.indexOfFirst { it.selected }
    LaunchedEffect(selected, episodes.size) {
        if (selected in episodes.indices) listState.scrollToItem(selected)
    }
    ModalBottomSheet(
        onDismissRequest = dismiss,
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
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                sheetState.hide()
                                onDismiss()
                                onOpenList()
                            }
                        }
                        .padding(4.dp),
                )
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
        ) {
            itemsIndexed(episodes, key = { index, _ -> index }) { index, item ->
                EpisodeRow(
                    name = item.name,
                    selected = item.selected,
                    index = index,
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                            onEpisode(index)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    name: String,
    selected: Boolean,
    index: Int,
    onClick: () -> Unit,
) {
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    val bg by animateColorAsState(
        targetValue = if (selected) EpisodeSelectedBg else EpisodeUnselectedBg,
        animationSpec = tween(SheetMotionMs, easing = FastOutSlowInEasing),
        label = "episodeBg",
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.Black else Color.White,
        animationSpec = tween(SheetMotionMs, easing = FastOutSlowInEasing),
        label = "episodeFg",
    )
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = SheetMotionMs,
                delayMillis = minOf(index, StaggerMaxIndex) * StaggerDelayMs,
                easing = FastOutSlowInEasing,
            ),
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = SheetMotionMs,
                delayMillis = minOf(index, StaggerMaxIndex) * StaggerDelayMs,
                easing = FastOutSlowInEasing,
            ),
            initialOffsetY = { it / 4 },
        ),
        exit = fadeOut(animationSpec = tween(0)),
    ) {
        Text(
            text = name,
            color = fg,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(bg, RoundedCornerShape(4.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
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
    val dismiss = rememberHideThenDismiss(sheetState, onDismiss)
    ModalBottomSheet(
        onDismissRequest = dismiss,
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
                TextButton(onClick = dismiss) {
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
    val dismiss = rememberHideThenDismiss(sheetState, onDismiss)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = dismiss,
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
                        .combinedClickable(
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    onDismiss()
                                    onShare()
                                }
                            },
                            onLongClick = {
                                Util.copy(info.url)
                            },
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlMoreSheet(
    visibility: VodControlMoreVisibility,
    onAction: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dismiss = rememberHideThenDismiss(sheetState, onDismiss)
    val scope = rememberCoroutineScope()
    fun run(id: Int) {
        scope.launch {
            sheetState.hide()
            onDismiss()
            onAction(id)
        }
    }
    val speedLabel = visibility.speedLabel.ifEmpty { stringResource(R.string.play_speed) }
    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = SheetContainer,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.control_more),
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
            ControlMoreRow(stringResource(R.string.detail_episode)) { run(VodControlMoreIds.EPISODES) }
            if (visibility.showSpeed) {
                ControlMoreRow(speedLabel) { run(VodControlMoreIds.SPEED) }
            }
            val scaleLabel = visibility.scaleLabel.ifEmpty { stringResource(R.string.play_scale) }
            ControlMoreRow(scaleLabel) { run(VodControlMoreIds.SCALE) }
            val decodeLabel = visibility.decodeLabel.ifEmpty { stringResource(R.string.play_decode) }
            ControlMoreRow(decodeLabel) { run(VodControlMoreIds.DECODE) }
            ControlMoreRow(
                label = stringResource(R.string.play_repeat) + if (visibility.repeatOn) " ✓" else "",
                onClick = { run(VodControlMoreIds.REPEAT) },
            )
            ControlMoreRow(visibility.openingLabel.ifEmpty { stringResource(R.string.play_op) }) {
                run(VodControlMoreIds.OPENING)
            }
            ControlMoreRow(visibility.endingLabel.ifEmpty { stringResource(R.string.play_ed) }) {
                run(VodControlMoreIds.ENDING)
            }
            if (visibility.showText) {
                ControlMoreRow(
                    label = stringResource(R.string.play_track_text),
                    onClick = { run(VodControlMoreIds.TEXT) },
                    onLongClick = { run(VodControlMoreIds.SUBTITLE) },
                )
            }
            if (visibility.showAudio) {
                ControlMoreRow(stringResource(R.string.play_track_audio)) { run(VodControlMoreIds.AUDIO) }
            }
            if (visibility.showVideo) {
                ControlMoreRow(stringResource(R.string.play_track_video)) { run(VodControlMoreIds.VIDEO) }
            }
            if (visibility.showDanmaku) {
                ControlMoreRow(stringResource(R.string.danmaku)) { run(VodControlMoreIds.DANMAKU) }
            }
            ControlMoreRow(stringResource(R.string.play_info)) { run(VodControlMoreIds.INFO) }
            ControlMoreRow(stringResource(R.string.play_replay)) { run(VodControlMoreIds.REPLAY) }
            ControlMoreRow(stringResource(R.string.play_reset)) { run(VodControlMoreIds.RESET) }
            ControlMoreRow(stringResource(R.string.play_config)) { run(VodControlMoreIds.ALL_SETTINGS) }
        }
    }
}

@Composable
private fun ControlMoreRow(
    label: String,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
                } else {
                    Modifier.clickable(onClick = onClick)
                },
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
    )
}
