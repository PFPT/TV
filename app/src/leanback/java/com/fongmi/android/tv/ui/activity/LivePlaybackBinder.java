package com.fongmi.android.tv.ui.activity;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Channel;
import com.fongmi.android.tv.bean.EpgData;
import com.fongmi.android.tv.bean.Group;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.playback.PlaybackReset;
import com.fongmi.android.tv.playback.live.LivePlayRequest;
import com.fongmi.android.tv.playback.live.LivePlaybackHost;
import com.fongmi.android.tv.utils.Notify;

import java.time.ZoneId;

class LivePlaybackBinder implements LivePlaybackHost {

    private final LiveActivity activity;

    LivePlaybackBinder(LiveActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getGroupCount() {
        return activity.mGroupAdapter.getItemCount();
    }

    @Override
    public int getGroupPosition() {
        return activity.mBinding.group.getSelectedPosition();
    }

    @Override
    public Group getGroup(int position) {
        return activity.mGroupAdapter.get(position);
    }

    @Override
    public boolean isPlayerLive() {
        return activity.player().isLive();
    }

    @Override
    public ZoneId getZoneId() {
        return activity.mViewModel.getZoneId();
    }

    @Override
    public void requestUrl(LivePlayRequest request) {
        activity.mViewModel.getUrl(request.getChannel(), request.getPosition());
    }

    @Override
    public void requestCatchupUrl(LivePlayRequest request) {
        activity.mViewModel.getUrl(request.getChannel(), request.getCatchupData(), request.getPosition());
        activity.hideUI();
    }

    @Override
    public void stopPlaybackForRefresh() {
        activity.stopPlayer();
    }

    @Override
    public void startPlayback(Result result, long position, Channel channel) {
        activity.start(result, position);
    }

    @Override
    public void resetPlaybackForError(String msg) {
        PlaybackReset.afterError(activity.player());
        activity.showError(msg);
    }

    @Override
    public void renderGroupSelection(Group group) {
        activity.mGroup = group;
        activity.mBinding.group.setSelectedPosition(activity.mGroupAdapter.indexOf(group));
    }

    @Override
    public void renderGroupChannels(Group group) {
        activity.mChannelAdapter.addAll(activity.setWidth(group).getChannel());
        activity.mBinding.channel.setSelectedPosition(Math.max(group.getPosition(), 0));
    }

    @Override
    public void renderChannelSelection(Channel channel) {
        App.post(activity.mR0, 100);
        activity.mChannel = channel;
        activity.setArtwork();
        activity.showInfo();
    }

    @Override
    public void renderLineSelection(Channel channel, boolean show) {
        if (show) activity.showInfo();
        else activity.setInfo();
    }

    @Override
    public void renderEpgSelection(Channel channel, EpgData data) {
        activity.setSelected(data);
    }

    @Override
    public void showCatchupReady(Channel channel, EpgData data) {
        activity.mBinding.widget.title.setText(activity.getString(R.string.detail_title, channel.getShow(), data.getTitle()));
        Notify.show(activity.getString(R.string.play_ready, data.getTitle()));
    }

    @Override
    public void showProgress() {
        activity.showProgress();
    }
}
