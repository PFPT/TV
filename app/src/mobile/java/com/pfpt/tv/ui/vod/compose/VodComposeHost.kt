package com.pfpt.tv.ui.vod.compose

import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pfpt.tv.playback.vod.session.VodSessionViewModel

fun interface VodIndexClick {
    fun onClick(index: Int)
}

fun interface VodSeek {
    fun onSeek(positionMs: Long)
}

fun interface VodScrub {
    fun onScrub(scrubbing: Boolean)
}

fun interface VodGestureDelta {
    fun onDelta(delta: Float)
}

fun interface VodGestureSeek {
    fun onPreview(deltaMs: Long)
}

fun interface VodGesturePinch {
    fun onPinch(factor: Float, focusX: Float, focusY: Float)
}

@JvmOverloads
fun ComposeView.setVodScreen(
    session: VodSessionViewModel,
    onKeepClick: Runnable,
    onTitleClick: Runnable,
    onTitleLongClick: Runnable,
    onSiteClick: Runnable,
    onRemarkClick: Runnable,
    onFlagClick: VodIndexClick? = null,
    onEpisodeClick: VodIndexClick? = null,
    onQualityClick: VodIndexClick? = null,
    showCatalog: Boolean = false,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodScreen(
            state = state,
            onKeepClick = onKeepClick::run,
            onTitleClick = onTitleClick::run,
            onTitleLongClick = onTitleLongClick::run,
            onSiteClick = onSiteClick::run,
            onRemarkClick = onRemarkClick::run,
            onFlagClick = { index -> onFlagClick?.onClick(index) },
            onEpisodeClick = { index -> onEpisodeClick?.onClick(index) },
            onQualityClick = { index -> onQualityClick?.onClick(index) },
            showCatalog = showCatalog,
        )
    }
}

fun setVodControlBars(
    top: ComposeView,
    center: ComposeView,
    lock: ComposeView,
    bottom: ComposeView,
    session: VodSessionViewModel,
    onBack: Runnable,
    onPlay: Runnable,
    onNext: Runnable,
    onPrev: Runnable,
    onLock: Runnable,
    onKeep: Runnable,
    onFullscreen: Runnable,
    onMore: Runnable,
    onSeek: VodSeek,
    onScrub: VodScrub,
    onCast: Runnable,
    onPip: Runnable,
    onRotate: Runnable,
    onDanmaku: Runnable,
) {
    top.setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        if (state.controlsVisible && !state.locked) {
            VodControlTopBar(
                state = state,
                onBack = onBack::run,
                onKeep = onKeep::run,
                onMore = onMore::run,
                onCast = onCast::run,
                onDanmaku = onDanmaku::run,
            )
        }
    }
    center.setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        if (state.controlsVisible && !state.locked) {
            VodControlCenter(
                state = state,
                onPlay = onPlay::run,
                onNext = onNext::run,
                onPrev = onPrev::run,
            )
        }
    }
    lock.setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        if (state.controlsVisible && state.fullscreen) {
            VodControlLock(locked = state.locked, onLock = onLock::run)
        }
    }
    bottom.setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        if (state.controlsVisible && !state.locked) {
            VodControlBottomBar(
                state = state,
                onSeek = onSeek::onSeek,
                onScrub = onScrub::onScrub,
                onFullscreen = onFullscreen::run,
                onPip = onPip::run,
                onRotate = onRotate::run,
            )
        }
    }
}

fun ComposeView.setVodGestureLayer(
    session: VodSessionViewModel,
    onBrightness: VodGestureDelta,
    onVolume: VodGestureDelta,
    onSeekPreview: VodGestureSeek,
    onSeekEnd: Runnable,
    onSingleTap: Runnable,
    onDoubleTap: Runnable,
    onSpeedUp: Runnable,
    onSpeedEnd: Runnable,
    onFlingUp: Runnable,
    onFlingDown: Runnable,
    onScale: VodGesturePinch,
    onTouchEnd: Runnable,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodGestureLayer(
            locked = state.locked,
            onBrightness = onBrightness::onDelta,
            onVolume = onVolume::onDelta,
            onSeekPreview = onSeekPreview::onPreview,
            onSeekEnd = onSeekEnd::run,
            onSingleTap = onSingleTap::run,
            onDoubleTap = onDoubleTap::run,
            onSpeedUp = onSpeedUp::run,
            onSpeedEnd = onSpeedEnd::run,
            onFlingUp = onFlingUp::run,
            onFlingDown = onFlingDown::run,
            onScale = onScale::onPinch,
            onTouchEnd = onTouchEnd::run,
        )
    }
}

fun ComposeView.setVodFlags(
    session: VodSessionViewModel,
    onFlagClick: VodIndexClick,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodChipRow(
            labels = state.flags.map { it.name },
            selectedIndex = state.flags.indexOfFirst { it.selected },
            onClick = onFlagClick::onClick,
        )
    }
}

fun ComposeView.setVodQualities(
    session: VodSessionViewModel,
    onQualityClick: VodIndexClick,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodQualityRow(
            qualities = state.qualities,
            qualityIndex = state.qualityIndex,
            onQualityClick = onQualityClick::onClick,
        )
    }
}

fun ComposeView.setVodEpisodes(
    session: VodSessionViewModel,
    onEpisodeClick: VodIndexClick,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodChipRow(
            labels = state.episodes.map { it.name },
            selectedIndex = state.episodes.indexOfFirst { it.selected },
            onClick = onEpisodeClick::onClick,
        )
    }
}
