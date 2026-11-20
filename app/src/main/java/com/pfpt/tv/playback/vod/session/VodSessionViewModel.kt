package com.pfpt.tv.playback.vod.session

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pfpt.tv.bean.Keep
import com.pfpt.tv.bean.Result
import com.pfpt.tv.playback.vod.VodPlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

fun interface KeepToggleCallback {
    fun onResult(kept: Boolean, removed: Boolean)
}

class VodSessionViewModel : ViewModel() {

    private val keepToggle: KeepToggle = KeepToggle.default()
    private val playbackState = AtomicReference<VodPlaybackState?>(null)
    private val _uiState = MutableStateFlow(VodUiState())
    val uiState: StateFlow<VodUiState> = _uiState.asStateFlow()
    val uiStateLiveData: LiveData<VodUiState> = _uiState.asLiveData()

    fun bind(state: VodPlaybackState?) {
        playbackState.set(state)
        refresh()
    }

    fun refresh() {
        val current = _uiState.value
        val mapped = VodUiStateMapper.from(
            playbackState.get(),
            current.kept,
            current.errorMessage,
            current.downloadReady,
            current.siteName,
            current.remark,
            current.flags,
            current.episodes,
            current.qualities,
            current.qualityIndex,
        )
        _uiState.value = mapped.copy(
            title = mapped.title.ifEmpty { current.title },
            controlsVisible = current.controlsVisible,
            locked = current.locked,
            playing = current.playing,
            positionMs = current.positionMs,
            durationMs = current.durationMs,
            speed = current.speed,
            fullscreen = current.fullscreen,
            danmakuOn = current.danmakuOn,
            showDanmaku = current.showDanmaku,
            showCast = current.showCast,
            showPip = current.showPip,
            showRotate = current.showRotate,
        )
    }

    fun updateDanmakuChrome(show: Boolean, on: Boolean) {
        _uiState.update {
            it.copy(showDanmaku = show, danmakuOn = on)
        }
    }

    fun updateControlVisibility(
        showCast: Boolean,
        showPip: Boolean,
        showRotate: Boolean,
        showDanmaku: Boolean,
        danmakuOn: Boolean,
    ) {
        _uiState.update {
            it.copy(
                showCast = showCast,
                showPip = showPip,
                showRotate = showRotate,
                showDanmaku = showDanmaku,
                danmakuOn = danmakuOn,
            )
        }
    }

    fun updatePlaybackChrome(
        controlsVisible: Boolean,
        locked: Boolean,
        playing: Boolean,
        positionMs: Long,
        durationMs: Long,
        speed: Float,
        fullscreen: Boolean,
    ) {
        _uiState.update {
            it.copy(
                controlsVisible = controlsVisible,
                locked = locked,
                playing = playing,
                positionMs = positionMs,
                durationMs = durationMs,
                speed = speed,
                fullscreen = fullscreen,
            )
        }
    }

    fun updatePlaybackProgress(positionMs: Long, durationMs: Long, playing: Boolean, speed: Float) {
        _uiState.update {
            it.copy(
                playing = playing,
                positionMs = positionMs,
                durationMs = durationMs,
                speed = speed,
            )
        }
    }

    fun updateCatalog(
        flags: List<VodFlagItem>,
        episodes: List<VodEpisodeItem>,
        qualities: List<String>,
        qualityIndex: Int,
    ) {
        _uiState.update {
            it.copy(
                flags = flags,
                episodes = episodes,
                qualities = qualities,
                qualityIndex = qualityIndex,
            )
        }
    }

    fun updateDetailMeta(title: String?, siteName: String?, remark: String?) {
        _uiState.update {
            it.copy(
                title = title ?: it.title,
                siteName = siteName ?: it.siteName,
                remark = remark ?: it.remark,
            )
        }
    }

    fun loadKept(key: String?) {
        if (key.isNullOrEmpty()) return
        viewModelScope.launch {
            val kept = withContext(Dispatchers.IO) {
                try {
                    Keep.find(key) != null
                } catch (_: Exception) {
                    _uiState.value.kept
                }
            }
            _uiState.update { it.copy(kept = kept) }
            refresh()
        }
    }

    fun toggleKeep(
        key: String?,
        cid: Int,
        vodName: String?,
        vodPic: String?,
        siteName: String?,
        callback: KeepToggleCallback?,
    ) {
        if (key.isNullOrEmpty()) return
        viewModelScope.launch {
            val previous = _uiState.value.kept
            val result = withContext(Dispatchers.IO) {
                try {
                    keepToggle.toggle(
                        KeepToggleRequest(
                            key = key,
                            cid = cid,
                            vodName = vodName.orEmpty(),
                            vodPic = vodPic.orEmpty(),
                            siteName = siteName.orEmpty(),
                        ),
                    )
                } catch (_: Exception) {
                    KeepToggleResult(kept = previous, removed = false)
                }
            }
            _uiState.update { it.copy(kept = result.kept, errorMessage = null) }
            refresh()
            callback?.onResult(result.kept, result.removed)
        }
    }

    fun prepareDownloadFromPlayer(
        url: String?,
        headers: Map<String, String>?,
        fileName: String,
    ): DownloadResolveOutcome.Ready? {
        val ready = VodDownloadResolver.fromPlayer(url, headers) ?: return null
        _uiState.update {
            it.copy(downloadReady = VodDownloadReadySummary(fileName = fileName, url = ready.url))
        }
        return ready
    }

    fun resolveDownloadResult(result: Result): DownloadResolveOutcome {
        return VodDownloadResolver.fromResult(result)
    }

    fun clearDownloadReady() {
        _uiState.update { it.copy(downloadReady = null) }
    }

    fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }
}
