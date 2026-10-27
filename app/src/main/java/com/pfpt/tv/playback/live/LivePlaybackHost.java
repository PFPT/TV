package com.pfpt.tv.playback.live;

import androidx.annotation.Nullable;

import com.pfpt.tv.bean.Channel;
import com.pfpt.tv.bean.EpgData;
import com.pfpt.tv.bean.Group;
import com.pfpt.tv.bean.Result;

import java.time.ZoneId;

public interface LivePlaybackHost {

    int getGroupCount();

    int getGroupPosition();

    Group getGroup(int position);

    boolean isPlayerLive();

    ZoneId getZoneId();

    @Nullable
    default EpgData getNextEpgData(Channel channel) {
        return LiveEpgPolicy.next(channel, getZoneId());
    }

    @Nullable
    default EpgData getCurrentEpgData(Channel channel) {
        return LiveEpgPolicy.current(channel, getZoneId());
    }

    void requestUrl(LivePlayRequest request);

    void requestCatchupUrl(LivePlayRequest request);

    void stopPlaybackForRefresh();

    void startPlayback(Result result, long position, Channel channel);

    void resetPlaybackForError(String msg);

    void renderGroupSelection(Group group);

    void renderGroupChannels(Group group);

    void renderChannelSelection(Channel channel);

    void renderLineSelection(Channel channel, boolean show);

    void renderEpgSelection(Channel channel, EpgData data);

    void showCatchupReady(Channel channel, EpgData data);

    void showProgress();
}
