package com.pfpt.tv.playback.live;

import androidx.media3.common.MediaMetadata;

import com.pfpt.tv.bean.Channel;
import com.pfpt.tv.player.PlayerManager;

public final class LivePlaybackMedia {

    public static MediaMetadata metadata(Channel channel, CharSequence artist) {
        String title = channel == null ? "" : channel.getShow();
        String logo = channel == null ? "" : channel.getLogo();
        return PlayerManager.buildMetadata(title, artist == null ? "" : artist.toString(), logo);
    }
}
