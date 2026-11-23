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
import com.pfpt.tv.utils.Util;

import java.util.List;

class VodPlaybackBinder extends AbstractVodPlaybackBinder {

    private final VideoActivity activity;

    VodPlaybackBinder(VideoActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void requestPlayer(VodPlayRequest request) {
        activity.mBinding.widget.title.setText(activity.getString(R.string.detail_title, activity.mBinding.name.getText(), request.getTitle()));
        activity.mViewModel.playerContent(request.getKey(), request.getFlag(), request.getId());
        activity.mBinding.widget.title.setSelected(true);
        activity.showProgress();
    }

    @Override
    public void prepareSource(Vod item) {
        activity.getIntent().putExtra("key", item.getSiteKey());
        activity.getIntent().putExtra("pic", item.getPic());
        activity.getIntent().putExtra("id", item.getId());
        activity.mBinding.scroll.scrollTo(0, 0);
        activity.mClock.setCallback(null);
        activity.updateNavigationKey();
        activity.player().reset();
        activity.player().stop();
    }

    @Override
    public void resetPlaybackForError(String msg) {
        PlaybackReset.afterError(activity.player(), () -> activity.mClock.setCallback(null));
        activity.showError(msg);
    }

    @Override
    public void renderDetail(Vod item, History history) {
        activity.mHistory = history;
        activity.mBinding.progressLayout.showContent();
        activity.mBinding.name.setText(item.getName());
        activity.mBinding.video.requestFocus();
        App.removeCallbacks(activity.mR4);
        activity.setArtwork(item.getPic());
        activity.checkKeepImg();
        activity.setText(item);
        activity.updateKeep();
    }

    @Override
    public void renderFallbackName(String name) {
        activity.mBinding.name.setText(name);
    }

    @Override
    public void renderFlags(List<Flag> items) {
        activity.mBinding.flag.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        activity.mFlagAdapter.addAll(items);
    }

    @Override
    public void renderFlagSelection(Flag item) {
        activity.mBinding.flag.setSelectedPosition(activity.mFlagAdapter.indexOf(item));
        activity.notifyItemChanged(activity.mBinding.flag, activity.mFlagAdapter);
    }

    @Override
    public void renderEpisodeSelection(Episode item) {
        activity.notifyItemChanged(activity.mBinding.episode, activity.mEpisodeAdapter);
        activity.mBinding.episode.setSelectedPosition(activity.mEpisodeAdapter.getPosition());
    }

    @Override
    public void renderReverseEpisodes(List<Episode> items, boolean scroll) {
        activity.setEpisodeAdapter(items);
        if (scroll) activity.mBinding.episode.setSelectedPosition(activity.mEpisodeAdapter.getPosition());
    }

    @Override
    public void renderQuality(Result result, boolean visible) {
        activity.mQualityAdapter.addAll(result);
        activity.setQualityVisible(visible);
    }

    @Override
    public void renderHistory(History history) {
        activity.mHistory = history;
        activity.mBinding.control.action.opening.setText(history.getOpening() <= 0 ? activity.getString(R.string.play_op) : Util.timeMs(history.getOpening()));
        activity.mBinding.control.action.ending.setText(history.getEnding() <= 0 ? activity.getString(R.string.play_ed) : Util.timeMs(history.getEnding()));
        PlaybackAction.setSpeed(activity.player(), activity.mBinding.control.action.speed, history.getSpeed());
        activity.setScale(activity.getScale());
        activity.setPartAdapter();
    }

    @Override
    public void renderUseParse(boolean useParse) {
        activity.setUseParse(useParse);
        activity.mBinding.control.action.parse.setVisibility(activity.isUseParse() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderDescription(String desc) {
        activity.mBinding.content.setTag(desc);
    }

    @Override
    public void onSearchStarted(String keyword) {
        activity.mBinding.part.setTag(keyword);
    }
}
