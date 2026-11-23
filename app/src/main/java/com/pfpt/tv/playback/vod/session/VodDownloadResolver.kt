package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.bean.Result
import java.util.Collections

sealed class DownloadResolveOutcome {
    data class Ready(
        val url: String,
        val headers: Map<String, String>,
    ) : DownloadResolveOutcome()

    data class NeedParse(
        val result: Result,
    ) : DownloadResolveOutcome()

    data class Message(
        val text: String,
    ) : DownloadResolveOutcome()

    data object EmptyUrl : DownloadResolveOutcome()
}

object VodDownloadResolver {

    @JvmStatic
    fun fromPlayer(url: String?, headers: Map<String, String>?): DownloadResolveOutcome.Ready? {
        if (url.isNullOrEmpty()) return null
        return DownloadResolveOutcome.Ready(url, headers ?: emptyMap())
    }

    @JvmStatic
    fun fromResult(result: Result): DownloadResolveOutcome {
        val message = runCatching { if (result.hasMsg()) result.msg else null }.getOrNull()
        if (!message.isNullOrEmpty()) return DownloadResolveOutcome.Message(message)
        val realUrl = normalizeUrl(runCatching { result.realUrl }.getOrNull())
        if (realUrl.isEmpty()) return DownloadResolveOutcome.EmptyUrl
        val needParse = runCatching { result.needParse() || result.isUseParse }.getOrDefault(false)
        if (needParse) return DownloadResolveOutcome.NeedParse(result)
        return DownloadResolveOutcome.Ready(realUrl, result.header ?: emptyMap())
    }

    private fun normalizeUrl(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return ""
        return raw
    }

    @JvmStatic
    fun buildFileName(title: String?, episode: String?): String {
        val name = title.orEmpty()
        val part = episode.orEmpty()
        if (name.isEmpty()) return part
        if (part.isEmpty()) return name
        return "$name-$part"
    }

    @JvmStatic
    fun emptyHeaders(): Map<String, String> = Collections.emptyMap()
}
