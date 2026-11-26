package com.pfpt.tv.ui.activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.C;
import androidx.media3.session.MediaController;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pfpt.tv.App;
import com.pfpt.tv.Constant;
import com.pfpt.tv.databinding.ActivityVideoBinding;
import com.pfpt.tv.playback.PlaybackOrientation;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.ui.vod.compose.VodPlayerScale;
import com.pfpt.tv.utils.PiP;
import com.pfpt.tv.utils.Traffic;

final class MobileVodChrome {

    interface Host {
        ActivityVideoBinding binding();

        PlayerManager player();

        boolean serviceReady();

        VodSessionViewModel session();

        PiP pip();

        VodPlayerScale playerScale();

        ViewGroup.LayoutParams frame();

        Runnable hideTask();

        Runnable trafficTask();

        Runnable orientTask();

        boolean fullscreen();

        void setFullscreen(boolean value);

        boolean rotate();

        void setRotate(boolean value);

        boolean locked();

        int scale();

        boolean port();

        boolean land();

        boolean autoRotate();

        boolean scrubbing();

        boolean inPip();

        boolean visible(View view);

        MediaController controller();

        void orientation(int value);

        void pad(ViewGroup view);

        void clearPad(ViewGroup view);

        ViewGroup[] controlViews();

        ViewGroup gesture();

        FragmentActivity activity();
    }

    private final Host host;

    MobileVodChrome(Host host) {
        this.host = host;
    }

    void enterFullscreen() {
        if (host.fullscreen()) return;
        host.setFullscreen(true);
        if (host.land() && !host.player().isPortrait()) setTransition();
        host.binding().video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        host.orientation(PlaybackOrientation.getEnterFullscreenOrientation(host.player().isPortrait()));
        host.setRotate(host.player().isPortrait());
        host.playerScale().reset();
        App.post(host.orientTask(), 2000);
        hideControl();
    }

    void exitFullscreen() {
        if (!host.fullscreen()) return;
        host.setFullscreen(false);
        if (host.land() && !host.player().isPortrait()) setTransition();
        host.orientation(PlaybackOrientation.getExitFullscreenOrientation(host.port()));
        host.binding().video.setLayoutParams(host.frame());
        host.playerScale().reset();
        App.post(host.orientTask(), 2000);
        host.setRotate(false);
        hideControl();
    }

    private void setTransition() {
        ChangeBounds transition = new ChangeBounds();
        transition.setDuration(150);
        ViewGroup parent = (ViewGroup) host.binding().video.getParent();
        TransitionManager.beginDelayedTransition(parent, transition);
    }

    int getLockOrient() {
        return PlaybackOrientation.getLockOrientation(host.activity(), host.locked(), host.rotate(), host.port() && host.autoRotate());
    }

    void showProgress() {
        host.binding().progress.getRoot().setVisibility(View.VISIBLE);
        App.post(host.trafficTask(), 0);
        hideError();
    }

    void hideProgress() {
        host.binding().progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(host.trafficTask());
        Traffic.reset();
    }

    void showError(String text) {
        VodSessionViewModel session = host.session();
        if (session != null) session.setErrorMessage(text);
        hideProgress();
    }

    private void hideError() {
        VodSessionViewModel session = host.session();
        if (session != null) session.setErrorMessage(null);
    }

    void syncDanmakuEnabled() {
        host.player().setDanmakuEnabled(DanmakuSetting.isShow());
    }

    void hideDanmaku() {
        host.player().setDanmakuEnabled(false);
    }

    boolean isControlOpen() {
        return host.visible(host.binding().controlDim);
    }

    private void setComposeControlVisible(boolean open) {
        ActivityVideoBinding binding = host.binding();
        binding.controlDim.setVisibility(open ? View.VISIBLE : View.GONE);
        binding.controlTopCompose.setVisibility(View.VISIBLE);
        binding.controlCenterCompose.setVisibility(View.VISIBLE);
        binding.controlBottomCompose.setVisibility(View.VISIBLE);
        binding.controlLockCompose.setVisibility(View.VISIBLE);
    }

    void padComposeControls(boolean pad) {
        for (ViewGroup view : host.controlViews()) {
            if (pad) host.pad(view);
            else host.clearPad(view);
        }
    }

    void showControl() {
        if (!host.serviceReady() || host.inPip()) return;
        setComposeControlVisible(true);
        applyChromeUi(true);
        setR1Callback();
    }

    void hideControl() {
        setComposeControlVisible(false);
        applyChromeUi(false);
        App.removeCallbacks(host.hideTask());
    }

    void updatePlayInfo() {
        applyChromeUi(isControlOpen());
    }

    private void applyChromeUi(boolean controlsVisible) {
        boolean fullscreen = host.fullscreen() && !host.inPip();
        if (fullscreen && !host.rotate()) host.pad(host.gesture());
        else host.clearPad(host.gesture());
        syncComposeChrome(controlsVisible);
    }

    void hideSheet() {
        host.activity().getSupportFragmentManager().getFragments().stream().filter(fragment -> fragment instanceof BottomSheetDialogFragment).map(fragment -> (BottomSheetDialogFragment) fragment).forEach(BottomSheetDialogFragment::dismiss);
    }

    void setTraffic() {
        Traffic.setSpeed(host.binding().progress.traffic);
        App.post(host.trafficTask(), 1000);
    }

    void setOrient() {
        if (host.port() && host.autoRotate()) host.orientation(PlaybackOrientation.getPortAutoRotateOrientation());
        if (host.land() && host.autoRotate()) host.orientation(PlaybackOrientation.getLandAutoRotateOrientation());
    }

    void setR1Callback() {
        if (host.scrubbing()) return;
        App.post(host.hideTask(), Constant.INTERVAL_HIDE);
    }

    void onPip() {
        PlayerManager player = host.player();
        host.pip().enter(host.activity(), player.getVideoWidth(), player.getVideoHeight(), host.scale(), true);
    }

    long readPositionMs() {
        try {
            MediaController controller = host.controller();
            if (controller != null) {
                long position = controller.getCurrentPosition();
                if (position >= 0) return position;
            }
            if (host.serviceReady()) {
                long position = host.player().getPosition();
                if (position >= 0) return position;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    long readDurationMs() {
        try {
            MediaController controller = host.controller();
            if (controller != null) {
                long duration = controller.getDuration();
                if (duration > 0 && duration != C.TIME_UNSET) return duration;
            }
            if (host.serviceReady()) {
                long duration = host.player().getDuration();
                if (duration > 0 && duration != C.TIME_UNSET) return duration;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    void updateMiniTime() {
        updateMiniTime(readPositionMs(), readDurationMs());
    }

    void syncComposeChrome(boolean visible) {
        VodSessionViewModel session = host.session();
        if (session == null) return;
        boolean fullscreen = host.fullscreen() && !host.inPip();
        session.updatePlaybackChrome(visible, host.locked(), readPlaying(), readPositionMs(), readDurationMs(), readSpeed(), fullscreen);
        pushComposeControlVisibility();
    }

    void pushPlaybackProgress(long position, long duration) {
        VodSessionViewModel session = host.session();
        if (session == null) return;
        long safePosition = position < 0 ? 0 : position;
        long safeDuration = duration < 0 || duration == C.TIME_UNSET ? 0 : duration;
        session.updatePlaybackProgress(safePosition, safeDuration, readPlaying(), readSpeed());
    }

    private boolean readPlaying() {
        try {
            return host.serviceReady() && host.player().isPlaying();
        } catch (Exception ignored) {
            return false;
        }
    }

    private float readSpeed() {
        try {
            return host.serviceReady() ? host.player().getSpeed() : 1f;
        } catch (Exception ignored) {
            return 1f;
        }
    }

    void onComposeSeek(long positionMs) {
        long target = Math.max(0, positionMs);
        MediaController controller = host.controller();
        if (controller != null) {
            controller.seekTo(target);
            controller.play();
        }
        updateMiniTime();
        setR1Callback();
    }

    void onComposeScrub(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(host.hideTask());
        else if (isControlOpen()) setR1Callback();
    }

    private void updateMiniTime(long position, long duration) {
        if (position < 0) position = 0;
        if (duration < 0 || duration == C.TIME_UNSET) duration = 0;
        pushPlaybackProgress(position, duration);
    }

    void pushComposeControlVisibility() {
        VodSessionViewModel session = host.session();
        if (session == null) return;
        boolean showCast = false;
        boolean showDanmaku = false;
        try {
            if (host.serviceReady()) {
                showCast = !host.player().isEmpty();
                showDanmaku = host.player().haveDanmaku();
            }
        } catch (Exception ignored) {
        }
        boolean showPip = !host.fullscreen() && !PiP.noPiP();
        boolean showRotate = host.fullscreen();
        session.updateControlVisibility(showCast, showPip, showRotate, showDanmaku, DanmakuSetting.isShow());
    }
}
