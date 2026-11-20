package com.pfpt.tv.playback.vod.session

data class VodDownloadReadySummary(
    val fileName: String = "",
    val url: String = "",
)

data class VodFlagItem(
    val name: String,
    val selected: Boolean,
)

data class VodEpisodeItem(
    val name: String,
    val selected: Boolean,
    val url: String,
)

data class VodUiState(
    val title: String = "",
    val siteName: String = "",
    val remark: String = "",
    val flagName: String = "",
    val episodeName: String = "",
    val kept: Boolean = false,
    val opening: Long = 0L,
    val ending: Long = 0L,
    val errorMessage: String? = null,
    val downloadReady: VodDownloadReadySummary? = null,
    val flags: List<VodFlagItem> = emptyList(),
    val episodes: List<VodEpisodeItem> = emptyList(),
    val qualities: List<String> = emptyList(),
    val qualityIndex: Int = 0,
    val controlsVisible: Boolean = false,
    val locked: Boolean = false,
    val playing: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val speed: Float = 1f,
    val fullscreen: Boolean = false,
    val danmakuOn: Boolean = false,
    val showDanmaku: Boolean = false,
    val showCast: Boolean = false,
    val showPip: Boolean = false,
    val showRotate: Boolean = false,
    val gestureBrightVisible: Boolean = false,
    val gestureBrightProgress: Int = 0,
    val gestureVolumeVisible: Boolean = false,
    val gestureVolumeProgress: Int = 0,
    val gestureSeekVisible: Boolean = false,
    val gestureSeekForward: Boolean = true,
    val gestureSeekTime: String = "",
    val gestureSpeedVisible: Boolean = false,
)
