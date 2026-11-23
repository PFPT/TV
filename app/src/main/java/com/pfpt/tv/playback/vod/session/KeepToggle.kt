package com.pfpt.tv.playback.vod.session

import com.pfpt.tv.bean.Keep

data class KeepToggleRequest(
    val key: String,
    val cid: Int,
    val vodName: String,
    val vodPic: String,
    val siteName: String,
)

data class KeepToggleResult(
    val kept: Boolean,
    val removed: Boolean,
)

class KeepToggle(
    private val exists: (String) -> Boolean,
    private val create: (KeepToggleRequest) -> Unit,
    private val delete: (String) -> Unit,
) {

    fun toggle(request: KeepToggleRequest): KeepToggleResult {
        val removed = exists(request.key)
        return if (removed) {
            delete(request.key)
            KeepToggleResult(kept = false, removed = true)
        } else {
            create(request)
            KeepToggleResult(kept = true, removed = false)
        }
    }

    companion object {
        @JvmStatic
        fun default(): KeepToggle {
            return KeepToggle(
                exists = { key -> Keep.find(key) != null },
                create = { req ->
                    Keep.createVod(req.key, req.cid, req.vodName, req.vodPic, req.siteName)
                },
                delete = { key ->
                    val keep = Keep.find(key)
                    keep?.delete()
                },
            )
        }
    }
}
