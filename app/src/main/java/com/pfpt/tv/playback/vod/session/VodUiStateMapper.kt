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
    ): VodUiState {
        if (state == null) {
            return VodUiState(
                kept = kept,
                siteName = siteName,
                remark = remark,
                errorMessage = errorMessage,
                downloadReady = downloadReady,
            )
        }
        val history = state.history
        val title = history?.vodName.orEmpty()
        val flagName = if (state.hasFlags()) state.getFlag().getFlag() else ""
        val episodeName = if (state.hasEpisode()) state.getEpisode().getName() else ""
        val opening = history?.opening ?: 0L
        val ending = history?.ending ?: 0L
        val flags = state.getFlags().map { flag ->
            val show = flag.getShow()
            VodFlagItem(
                if (show.isNullOrEmpty()) flag.getFlag() else show,
                flag.isSelected(),
            )
        }
        val episodes = if (state.hasFlags()) {
            state.getFlag().getEpisodes().map {
                VodEpisodeItem(
                    it.getDesc().orEmpty() + it.getName().orEmpty(),
                    it.isSelected(),
                    it.getUrl().orEmpty(),
                )
            }
        } else {
            emptyList()
        }
        val url = state.getQuality().getUrl()
        val showQualities = state.hasEpisode() && state.getEpisode().isSelected() && url.isMulti()
        val qualities = if (showQualities) {
            (0 until url.getValues().size).map { url.n(it).orEmpty() }
        } else {
            emptyList()
        }
        val qualityIndex = if (showQualities) url.getPosition() else 0
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
