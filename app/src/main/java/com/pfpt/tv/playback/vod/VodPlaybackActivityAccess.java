package com.pfpt.tv.playback.vod;

import android.content.Context;
import android.content.Intent;

import androidx.media3.common.MediaMetadata;

import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.player.PlayerManager;

import java.util.List;

public interface VodPlaybackActivityAccess {

    Context getHostContext();

    Intent getIntent();

    String getKey();

    String getId();

    String getName();

    String getPic();

    String getMark();

    String getHistoryKey();

    Site getSite();

    String getNameText();

    boolean isFinishing();

    boolean isDestroyed();

    boolean isFullscreen();

    void finish();

    PlayerManager player();

    void startPlayer(String key, Result result, boolean useParse, long timeout, long startPositionMs, MediaMetadata metadata);

    void detailContent(String key, String id);

    void searchContent(List<Site> sites, String keyword, boolean quick);

    void clearQuickAdapter();

    void addQuickItems(List<Vod> items);

    boolean isQuickAdapterEmpty();

    void setQuickVisible(int visibility);

    void setEpisodeAdapter(List<Episode> items);

    void setQualityVisible(boolean visible);

    void setArtwork(String url);

    void showEmpty();

    void clearClockCallback();

    void postFallbackRunnable(long delayMs);

    void removeFallbackRunnable();
}
