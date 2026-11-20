package com.pfpt.tv.ui.activity;

import android.view.View;

import com.pfpt.tv.App;
import com.pfpt.tv.R;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.PlaybackReset;
import com.pfpt.tv.playback.vod.AbstractVodPlaybackBinder;
import com.pfpt.tv.playback.vod.VodPlayRequest;

import java.util.List;

class VodPlaybackBinder extends AbstractVodPlaybackBinder {

    private final VideoActivity activity;

    VodPlaybackBinder(VideoActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void requestPlayer(VodPlayRequest request) {
        activity.setPlayTitle(activity.getString(R.string.detail_title, activity.mBinding.name.getText(), request.getTitle()));
        activity.mViewModel.playerContent(request.getKey(), request.getFlag(), request.getId());
        activity.showProgress();
    }

    @Override
    public void prepareSource(Vod item) {
        activity.getIntent().putExtra("key", item.getSiteKey());
        activity.getIntent().putExtra("pic", item.getPic());
        activity.getIntent().putExtra("id", item.getId());
        activity.mBinding.swipeLayout.setRefreshing(true);
        activity.mBinding.swipeLayout.setEnabled(false);
        activity.mBinding.scroll.scrollTo(0, 0);
        activity.mProgressClock.setCallback(null);
        activity.updateNavigationKey();
        activity.player().reset();
        activity.player().stop();
    }

    @Override
    public void resetPlaybackForError(String msg) {
        activity.mBinding.swipeLayout.setEnabled(true);
        PlaybackReset.afterError(activity.player(), () -> activity.mProgressClock.setCallback(null));
        activity.showError(msg);
    }

    @Override
    public void renderDetail(Vod item, History history) {
        activity.mHistory = history;
        activity.mBinding.swipeLayout.setRefreshing(false);
        activity.mBinding.progressLayout.showContent();
        activity.hideDetailHeaderXml();
        activity.mBinding.name.setText(item.getName());
        activity.updateDetailMeta(item.getName(), null, null);
        App.removeCallbacks(activity.mR4);
        activity.setArtwork(item.getPic());
        activity.checkKeepImg();
        activity.setText(item);
        activity.updateKeep();
    }

    @Override
    public void renderFallbackName(String name) {
        activity.mBinding.name.setText(name);
        activity.mBinding.name.setVisibility(View.GONE);
        activity.updateDetailMeta(name, null, null);
    }

    @Override
    public void renderFlags(List<Flag> items) {
        activity.mBinding.flag.setVisibility(View.GONE);
        activity.mFlagAdapter.addAll(items);
        activity.syncCatalog();
    }

    @Override
    public void renderFlagSelection(Flag item) {
        activity.notifyItemChanged(activity.mBinding.flag, activity.mFlagAdapter);
        activity.scrollToPosition(activity.mBinding.flag, activity.mFlagAdapter.getPosition());
        activity.syncCatalog();
    }

    @Override
    public void renderEpisodeSelection(Episode item) {
        activity.notifyItemChanged(activity.mBinding.episode, activity.mEpisodeAdapter);
        activity.scrollToPosition(activity.mBinding.episode, activity.mEpisodeAdapter.getPosition());
        activity.syncCatalog();
    }

    @Override
    public void renderReverseEpisodes(List<Episode> items, boolean scroll) {
        activity.setEpisodeAdapter(items);
        if (scroll) activity.scrollToPosition(activity.mBinding.episode, activity.mEpisodeAdapter.getPosition());
    }

    @Override
    public void renderQuality(Result result, boolean visible) {
        activity.mBinding.swipeLayout.setRefreshing(false);
        activity.mQualityAdapter.addAll(result);
        activity.setQualityVisible(visible);
    }

    @Override
    public void renderHistory(History history) {
        activity.mHistory = history;
        PlaybackAction.setSpeed(activity.player(), history.getSpeed());
        activity.setScale(activity.getScale());
    }

    @Override
    public void renderUseParse(boolean useParse) {
        activity.setUseParse(useParse);
    }

    @Override
    public void renderDescription(String desc) {
        activity.setText(activity.mBinding.content, 0, desc);
    }

    @Override
    public void onSearchStarted(String keyword) {
    }
}
