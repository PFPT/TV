package com.pfpt.tv.ui.activity;

import android.app.Activity;
import android.media.AudioManager;
import android.view.View;
import android.view.WindowManager;

import com.pfpt.tv.setting.LiveSetting;
import com.pfpt.tv.ui.vod.compose.VodPlayerScale;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Util;

public final class MobileVodGesture {

    private boolean brightArmed;
    private float bright;
    private boolean volumeArmed;
    private float volume;
    private int volumeMax;
    private boolean seeking;
    private long seekMs;
    private boolean animating;
    private AudioManager audio;

    public int brightness(Activity activity, int height, float delta) {
        if (!brightArmed) {
            bright = Util.getBrightness(activity);
            brightArmed = true;
        }
        if (height <= 0) return -1;
        float brightness = delta * 2f / height + bright;
        if (brightness < 0f) brightness = 0f;
        if (brightness > 1f) brightness = 1f;
        WindowManager.LayoutParams attributes = activity.getWindow().getAttributes();
        attributes.screenBrightness = brightness;
        activity.getWindow().setAttributes(attributes);
        return (int) (brightness * 100);
    }

    public int volume(Activity activity, int height, float delta) {
        if (audio == null) audio = (AudioManager) activity.getSystemService(Activity.AUDIO_SERVICE);
        if (audio == null) return -1;
        if (!volumeArmed) {
            volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumeMax = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            volumeArmed = true;
        }
        if (height <= 0 || volumeMax <= 0) return -1;
        float index = volume + delta * 2f / height * volumeMax;
        if (index > volumeMax) index = volumeMax;
        if (index < 0f) index = 0f;
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
        return (int) (index / volumeMax * 100f);
    }

    public long seekPreview(long deltaMs) {
        seeking = true;
        seekMs = deltaMs;
        return deltaMs;
    }

    public long seekEnd() {
        if (!seeking) return Long.MIN_VALUE;
        long delta = seekMs;
        seeking = false;
        return delta;
    }

    public void scale(VodPlayerScale playerScale, boolean locked, float factor, float focusX, float focusY) {
        if (locked || playerScale == null) return;
        playerScale.apply(factor, focusX, focusY);
    }

    public void fling(View player, boolean up, Runnable action) {
        if (player == null || animating) return;
        boolean invert = LiveSetting.isInvert();
        int dp = up ? (invert ? 24 : -24) : (invert ? -24 : 24);
        player.animate().translationYBy(ResUtil.dp2px(dp)).setDuration(150).withStartAction(() -> animating = true).withEndAction(() -> player.animate().translationY(0).setDuration(100).withStartAction(action).withEndAction(() -> animating = false).start()).start();
    }

    public void reset() {
        brightArmed = false;
        volumeArmed = false;
    }
}
