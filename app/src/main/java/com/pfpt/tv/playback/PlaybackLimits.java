package com.pfpt.tv.playback;

import com.pfpt.tv.Constant;

public final class PlaybackLimits {

    private PlaybackLimits() {
    }

    public static boolean canSetOpening(long position, long duration) {
        return position > 0 && duration > 0 && position <= Constant.getOpEdLimit(duration);
    }

    public static boolean canSetEnding(long position, long duration) {
        return position > 0 && duration > 0 && duration - position <= Constant.getOpEdLimit(duration);
    }
}
