package com.pfpt.tv.playback.vod;

import android.content.Context;

import com.pfpt.tv.R;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.utils.Notify;

public final class VodPlaybackNotify {

    private VodPlaybackNotify() {
    }

    public static void detailMessage(String msg) {
        Notify.show(msg);
    }

    public static void switchFlag(Context context, Flag flag) {
        Notify.show(context.getString(R.string.play_switch_flag, flag.getFlag()));
    }

    public static void switchSource(Context context, Vod item) {
        Notify.show(context.getString(R.string.play_switch_site, item.getSiteName()));
    }

    public static void episodeReady(Context context, Episode item) {
        Notify.show(context.getString(R.string.play_ready, item.getName()));
    }

    public static void noNext(boolean reversed) {
        Notify.show(reversed ? R.string.error_play_prev : R.string.error_play_next);
    }

    public static void noPrev(boolean reversed) {
        Notify.show(reversed ? R.string.error_play_next : R.string.error_play_prev);
    }
}
