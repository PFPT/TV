package com.pfpt.tv.ui.vod.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pfpt.tv.playback.vod.session.VodSessionViewModel

private const val CONTROL_MOTION_MS = 200

private val controlFadeIn = fadeIn(animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing))
private val controlFadeOut = fadeOut(animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing))
private val controlSlideInFromTop = slideInVertically(
    animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing),
    initialOffsetY = { -it / 4 },
)
private val controlSlideOutToTop = slideOutVertically(
    animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing),
    targetOffsetY = { -it / 4 },
)
private val controlSlideInFromBottom = slideInVertically(
    animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing),
    initialOffsetY = { it / 4 },
)
private val controlSlideOutToBottom = slideOutVertically(
    animationSpec = tween(CONTROL_MOTION_MS, easing = FastOutSlowInEasing),
    targetOffsetY = { it / 4 },
)

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
    onDownloadClick: Runnable,
    onReverseClick: Runnable,
    onMoreClick: Runnable,
    onCopyContent: Runnable,
    onFlagClick: VodIndexClick,
    onQualityClick: VodIndexClick,
    onEpisodeClick: VodIndexClick,
    onQuickClick: VodIndexClick,
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
            onDownloadClick = onDownloadClick::run,
            onReverseClick = onReverseClick::run,
            onMoreClick = onMoreClick::run,
            onCopyContent = onCopyContent::run,
            onFlagClick = onFlagClick::onClick,
            onQualityClick = onQualityClick::onClick,
            onEpisodeClick = onEpisodeClick::onClick,
            onQuickClick = onQuickClick::onClick,
        )
    }
}

fun ComposeView.setVodDetailHeader(
    session: VodSessionViewModel,
    onKeepClick: Runnable,
    onTitleClick: Runnable,
    onTitleLongClick: Runnable,
    onSiteClick: Runnable,
    onRemarkClick: Runnable,
    onDownloadClick: Runnable,
    onReverseClick: Runnable,
    onMoreClick: Runnable,
    onCopyContent: Runnable,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            VodDetailHeader(
                state = state,
                onKeepClick = onKeepClick::run,
                onTitleClick = onTitleClick::run,
                onTitleLongClick = onTitleLongClick::run,
                onSiteClick = onSiteClick::run,
                onRemarkClick = onRemarkClick::run,
                onDownloadClick = onDownloadClick::run,
                onReverseClick = onReverseClick::run,
                onMoreClick = onMoreClick::run,
                onCopyContent = onCopyContent::run,
            )
        }
    }
}

fun ComposeView.setVodDetailCatalog(
    session: VodSessionViewModel,
    onFlagClick: VodIndexClick,
    onQualityClick: VodIndexClick,
    onEpisodeClick: VodIndexClick,
    onQuickClick: VodIndexClick,
) {
    setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        VodDetailCatalog(
            state = state,
            onFlagClick = onFlagClick::onClick,
            onQualityClick = onQualityClick::onClick,
            onEpisodeClick = onEpisodeClick::onClick,
            onQuickClick = onQuickClick::onClick,
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
        AnimatedVisibility(
            visible = state.controlsVisible && !state.locked,
            enter = controlFadeIn + controlSlideInFromTop,
            exit = controlFadeOut + controlSlideOutToTop,
        ) {
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
        AnimatedVisibility(
            visible = state.controlsVisible && !state.locked,
            enter = controlFadeIn,
            exit = controlFadeOut,
        ) {
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
        AnimatedVisibility(
            visible = state.controlsVisible && state.fullscreen,
            enter = controlFadeIn,
            exit = controlFadeOut,
        ) {
            VodControlLock(locked = state.locked, onLock = onLock::run)
        }
    }
    bottom.setContent {
        val state by session.uiState.collectAsStateWithLifecycle()
        AnimatedVisibility(
            visible = state.controlsVisible && !state.locked,
            enter = controlFadeIn + controlSlideInFromBottom,
            exit = controlFadeOut + controlSlideOutToBottom,
        ) {
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
        Box(Modifier.fillMaxSize()) {
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
            VodGestureHud(state = state)
            VodStatusHud(state = state)
        }
    }
}

