package com.pfpt.tv.playback.vod;

import android.view.View;

import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Vod;

import java.util.List;

public abstract class AbstractVodPlaybackBinder implements VodPlaybackHost {

    protected final VodPlaybackActivityAccess access;

    protected AbstractVodPlaybackBinder(VodPlaybackActivityAccess access) {
        this.access = access;
    }

    @Override
    public String getVodKey() {
        return access.getKey();
    }

    @Override
    public String getVodId() {
        return access.getId();
    }

    @Override
    public String getVodName() {
        String name = access.getNameText();
        return name.isEmpty() ? access.getName() : name;
    }

    @Override
    public String getVodPic() {
        return access.getPic();
    }

    @Override
    public String getVodMark() {
        return access.getMark();
    }

    @Override
    public String getHistoryKey() {
        return access.getHistoryKey();
    }

    @Override
    public boolean isSiteChangeable() {
        return access.getSite().isChangeable();
    }

    @Override
    public boolean isFromCollect() {
        return access.getIntent().getBooleanExtra("collect", false);
    }

    @Override
    public boolean isHostFinishing() {
        return access.isFinishing() || access.isDestroyed();
    }

    @Override
    public boolean isPlayerEmpty() {
        return access.player().isEmpty();
    }

    @Override
    public boolean isFullscreenForPlayback() {
        return access.isFullscreen();
    }

    @Override
    public long getPlayerPosition() {
        return access.player().getPosition();
    }

    @Override
    public long getPlayerDuration() {
        return access.player().getDuration();
    }

    @Override
    public void usePushId(String id) {
        access.getIntent().putExtra("key", SiteApi.PUSH).putExtra("id", id);
    }

    @Override
    public void requestDetail(String key, String id) {
        access.detailContent(key, id);
    }

    @Override
    public void requestSearch(List<Site> sites, String keyword) {
        access.clearQuickAdapter();
        access.searchContent(sites, keyword, true);
    }

    @Override
    public void stopPlaybackForRefresh() {
        access.player().stop();
        access.player().clear();
        access.clearClockCallback();
    }

    @Override
    public void replay(long position) {
        access.player().replay(position);
    }

    @Override
    public void startPlayback(Result result, boolean useParse, long startPositionMs, History history, Episode episode) {
        access.startPlayer(access.getHistoryKey(), result, useParse, access.getSite().getTimeout(), startPositionMs, VodPlaybackMedia.metadata(history, episode));
    }

    @Override
    public void loadDanmaku(Result result, History history, Episode episode) {
        VodPlaybackMedia.searchDanmaku(result, history, episode, access.player()::setDanmaku, access.player()::addDanmaku);
    }

    @Override
    public void renderEmptyDetail() {
        access.showEmpty();
    }

    @Override
    public void renderEpisodes(List<Episode> items) {
        access.setEpisodeAdapter(items);
    }

    @Override
    public void renderQualityVisible(boolean visible) {
        access.setQualityVisible(visible);
    }

    @Override
    public void renderSources(List<Vod> items) {
        access.addQuickItems(items);
        access.setQuickVisible(access.isQuickAdapterEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void renderArtwork(String url) {
        access.setArtwork(url);
    }

    @Override
    public void onDetailFallbackScheduled() {
        access.postFallbackRunnable(10000);
    }

    @Override
    public void onDetailFallbackCancelled() {
        access.removeFallbackRunnable();
    }

    @Override
    public void onSearchResult() {
        access.removeFallbackRunnable();
    }

    @Override
    public void showDetailMessage(String msg) {
        VodPlaybackNotify.detailMessage(msg);
    }

    @Override
    public void showSwitchLine(Flag flag) {
        VodPlaybackNotify.switchFlag(access.getHostContext(), flag);
    }

    @Override
    public void showSwitchSource(Vod item) {
        VodPlaybackNotify.switchSource(access.getHostContext(), item);
    }

    @Override
    public void showEpisodeReady(Episode item) {
        VodPlaybackNotify.episodeReady(access.getHostContext(), item);
    }

    @Override
    public void showNoNext(boolean reversed) {
        VodPlaybackNotify.noNext(reversed);
    }

    @Override
    public void showNoPrev(boolean reversed) {
        VodPlaybackNotify.noPrev(reversed);
    }

    @Override
    public void finishVod() {
        access.finish();
    }
}
