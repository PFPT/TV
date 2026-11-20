package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.playback.vod.VodPlaybackState

object VodUiStateMapper {

    @JvmStatic
    fun from(
        state: VodPlaybackState?,
        kept: Boolean,
        errorMessage: String?,
        downloadReady: VodDownloadReadySummary?,
        siteName: String = "",
        remark: String = "",
        flags: List<VodFlagItem> = emptyList(),
        episodes: List<VodEpisodeItem> = emptyList(),
        qualities: List<String> = emptyList(),
        qualityIndex: Int = 0,
    ): VodUiState {
        if (state == null) {
            return VodUiState(
                kept = kept,
                siteName = siteName,
                remark = remark,
                errorMessage = errorMessage,
                downloadReady = downloadReady,
                flags = flags,
                episodes = episodes,
                qualities = qualities,
                qualityIndex = qualityIndex,
            )
        }
        val history = state.history
        val title = history?.vodName.orEmpty()
        val flagName = if (state.hasFlags()) state.flag?.flag.orEmpty() else ""
        val episodeName = if (state.hasEpisode()) state.episode?.name.orEmpty() else ""
        val opening = history?.opening ?: 0L
        val ending = history?.ending ?: 0L
        return VodUiState(
            title = title,
            siteName = siteName,
            remark = remark,
            flagName = flagName,
            episodeName = episodeName,
            kept = kept,
            opening = opening,
            ending = ending,
            errorMessage = errorMessage,
            downloadReady = downloadReady,
            flags = flags,
            episodes = episodes,
            qualities = qualities,
            qualityIndex = qualityIndex,
        )
    }
}
