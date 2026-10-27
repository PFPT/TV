package com.pfpt.tv.ui.activity;

import android.view.View;

import com.pfpt.tv.App;
import com.pfpt.tv.R;
import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.PlaybackReset;
import com.pfpt.tv.playback.vod.VodPlayRequest;
import com.pfpt.tv.playback.vod.VodPlaybackHost;
import com.pfpt.tv.playback.vod.VodPlaybackMedia;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.Util;

import java.util.List;

class VodPlaybackBinder implements VodPlaybackHost {

    private final VideoActivity activity;

    VodPlaybackBinder(VideoActivity activity) {
        this.activity = activity;
    }

    @Override
    public String getVodKey() {
        return activity.getKey();
    }

    @Override
    public String getVodId() {
        return activity.getId();
    }

    @Override
    public String getVodName() {
        String name = activity.mBinding.name.getText().toString();
        return name.isEmpty() ? activity.getName() : name;
    }

    @Override
    public String getVodPic() {
        return activity.getPic();
    }

    @Override
    public String getVodMark() {
        return activity.getMark();
    }

    @Override
    public String getHistoryKey() {
        return activity.getHistoryKey();
    }

    @Override
    public boolean isSiteChangeable() {
        return activity.getSite().isChangeable();
    }

    @Override
    public boolean isFromCollect() {
        return activity.getIntent().getBooleanExtra("collect", false);
    }

    @Override
    public boolean isHostFinishing() {
        return activity.isFinishing() || activity.isDestroyed();
    }

    @Override
    public boolean isPlayerEmpty() {
        return activity.player().isEmpty();
    }

    @Override
    public boolean isFullscreenForPlayback() {
        return activity.isFullscreen();
    }

    @Override
    public long getPlayerPosition() {
        return activity.player().getPosition();
    }

    @Override
    public void usePushId(String id) {
        activity.getIntent().putExtra("key", SiteApi.PUSH).putExtra("id", id);
    }

    @Override
    public void requestDetail(String key, String id) {
        activity.mViewModel.detailContent(key, id);
    }

    @Override
    public void requestPlayer(VodPlayRequest request) {
        activity.mBinding.control.title.setText(activity.getString(R.string.detail_title, activity.mBinding.name.getText(), request.getTitle()));
        activity.mViewModel.playerContent(request.getKey(), request.getFlag(), request.getId());
        activity.mBinding.control.title.setSelected(true);
        activity.showProgress();
    }

    @Override
    public void requestSearch(List<Site> sites, String keyword) {
        activity.mQuickAdapter.clear();
        activity.mViewModel.searchContent(sites, keyword, true);
    }

    @Override
    public void prepareSource(Vod item) {
        activity.getIntent().putExtra("key", item.getSiteKey());
        activity.getIntent().putExtra("pic", item.getPic());
        activity.getIntent().putExtra("id", item.getId());
        activity.mBinding.swipeLayout.setRefreshing(true);
        activity.mBinding.swipeLayout.setEnabled(false);
        activity.mBinding.scroll.scrollTo(0, 0);
        activity.mClock.setCallback(null);
        activity.updateNavigationKey();
        activity.player().reset();
        activity.player().stop();
    }

    @Override
    public void stopPlaybackForRefresh() {
        activity.player().stop();
        activity.player().clear();
        activity.mClock.setCallback(null);
    }

    @Override
    public void resetPlaybackForError(String msg) {
        activity.mBinding.swipeLayout.setEnabled(true);
        PlaybackReset.afterError(activity.player(), () -> activity.mClock.setCallback(null));
        activity.showError(msg);
    }

    @Override
    public void replay(long position) {
        activity.player().replay(position);
    }

    @Override
    public void startPlayback(Result result, boolean useParse, long startPositionMs, History history, Episode episode) {
        activity.startPlayer(activity.getHistoryKey(), result, useParse, activity.getSite().getTimeout(), startPositionMs, VodPlaybackMedia.metadata(history, episode));
    }

    @Override
    public void loadDanmaku(Result result, History history, Episode episode) {
        VodPlaybackMedia.searchDanmaku(result, history, episode, activity.player()::setDanmaku, activity.player()::addDanmaku);
    }

    @Override
    public void renderDetail(Vod item, History history) {
        activity.mHistory = history;
        activity.mBinding.swipeLayout.setRefreshing(false);
        activity.mBinding.progressLayout.showContent();
        activity.mBinding.name.setText(item.getName());
        App.removeCallbacks(activity.mR4);
        activity.setArtwork(item.getPic());
        activity.checkKeepImg();
        activity.setText(item);
        activity.updateKeep();
    }

    @Override
    public void renderEmptyDetail() {
        activity.showEmpty();
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
    public void renderEpisodes(List<Episode> items) {
        activity.setEpisodeAdapter(items);
    }

    @Override
    public void renderFlagSelection(Flag item) {
        activity.notifyItemChanged(activity.mBinding.flag, activity.mFlagAdapter);
        activity.scrollToPosition(activity.mBinding.flag, activity.mFlagAdapter.getPosition());
    }

    @Override
    public void renderEpisodeSelection(Episode item) {
        activity.notifyItemChanged(activity.mBinding.episode, activity.mEpisodeAdapter);
        activity.scrollToPosition(activity.mBinding.episode, activity.mEpisodeAdapter.getPosition());
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
    public void renderQualityVisible(boolean visible) {
        activity.setQualityVisible(visible);
    }

    @Override
    public void renderSources(List<Vod> items) {
        activity.mQuickAdapter.addAll(items);
        activity.mBinding.quick.setVisibility(activity.mQuickAdapter.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void renderHistory(History history) {
        activity.mHistory = history;
        activity.mBinding.control.action.opening.setText(history.getOpening() <= 0 ? activity.getString(R.string.play_op) : Util.timeMs(history.getOpening()));
        activity.mBinding.control.action.ending.setText(history.getEnding() <= 0 ? activity.getString(R.string.play_ed) : Util.timeMs(history.getEnding()));
        PlaybackAction.setSpeed(activity.player(), activity.mBinding.control.action.speed, history.getSpeed());
        activity.setScale(activity.getScale());
    }

    @Override
    public void renderUseParse(boolean useParse) {
        activity.setUseParse(useParse);
        activity.mBinding.control.action.parse.setVisibility(activity.isUseParse() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void renderArtwork(String url) {
        activity.setArtwork(url);
    }

    @Override
    public void renderDescription(String desc) {
        activity.setText(activity.mBinding.content, 0, desc);
    }

    @Override
    public void onDetailFallbackScheduled() {
        App.post(activity.mR4, 10000);
    }

    @Override
    public void onDetailFallbackCancelled() {
        App.removeCallbacks(activity.mR4);
    }

    @Override
    public void onSearchStarted(String keyword) {
    }

    @Override
    public void onSearchResult() {
        App.removeCallbacks(activity.mR4);
    }

    @Override
    public void showDetailMessage(String msg) {
        Notify.show(msg);
    }

    @Override
    public void showSwitchLine(Flag flag) {
        Notify.show(activity.getString(R.string.play_switch_flag, flag.getFlag()));
    }

    @Override
    public void showSwitchSource(Vod item) {
        Notify.show(activity.getString(R.string.play_switch_site, item.getSiteName()));
    }

    @Override
    public void showEpisodeReady(Episode item) {
        Notify.show(activity.getString(R.string.play_ready, item.getName()));
    }

    @Override
    public void showNoNext(boolean reversed) {
        Notify.show(reversed ? R.string.error_play_prev : R.string.error_play_next);
    }

    @Override
    public void showNoPrev(boolean reversed) {
        Notify.show(reversed ? R.string.error_play_next : R.string.error_play_prev);
    }

    @Override
    public void finishVod() {
        activity.finish();
    }
}
