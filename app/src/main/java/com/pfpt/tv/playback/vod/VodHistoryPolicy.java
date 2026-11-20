package com.pfpt.tv.playback.vod;

import android.text.TextUtils;

import androidx.media3.common.C;

import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.event.RefreshEvent;
import com.pfpt.tv.setting.Setting;
import com.pfpt.tv.utils.Task;

import java.util.Map;

public class VodHistoryPolicy {

    private final Map<String, long[]> episodeProgress;

    public VodHistoryPolicy(Map<String, long[]> episodeProgress) {
        this.episodeProgress = episodeProgress;
    }

    public History findOrCreate(String key, String mark, Vod item) {
        History history = History.find(key);
        history = history == null ? create(key, item) : history;
        if (!TextUtils.isEmpty(mark)) history.setVodRemarks(mark);
        if (Setting.isIncognito() && history.getKey().equals(key)) history.delete();
        history.setVodName(item.getName());
        return history;
    }

    private History create(String key, Vod item) {
        History history = new History();
        history.setKey(key);
        history.setCid(VodConfig.getCid());
        history.setVodName(item.getName());
        history.findEpisode(item.getFlags());
        return history;
    }

    public void save(History history) {
        save(history, false);
    }

    public void save(History history, boolean exit) {
        if (history != null && history.canSave() && !Setting.isIncognito()) Task.execute(() -> {
            history.merge().save();
            if (exit) RefreshEvent.history();
        });
    }

    public void sync(History history) {
        if (history != null && !Setting.isIncognito()) Task.execute(history::save);
    }

    public void capture(History history, long position, long duration) {
        if (history == null || position <= 0) return;
        history.setPosition(position);
        if (duration > 0) history.setDuration(duration);
        remember(history);
    }

    public void clearPosition(History history) {
        if (history == null) return;
        episodeProgress.remove(key(history));
        history.setPosition(C.TIME_UNSET);
        history.setDuration(C.TIME_UNSET);
    }

    public void updateEpisode(History history, Flag flag, Episode episode) {
        if (history == null || flag == null || episode == null) return;
        if (!sameEpisode(history, flag, episode)) {
            remember(history);
            restore(history, flag, episode);
        }
        history.setVodFlag(flag.getFlag());
        history.setVodRemarks(episode.getName());
        history.setEpisodeUrl(episode.getUrl());
    }

    public void updateTime(History history, long time, long position, long duration) {
        if (history == null || position < 0 || duration <= 0) return;
        history.setCreateTime(time);
        history.setPosition(position);
        history.setDuration(duration);
        remember(history);
        if (history.canSave() && history.canSync()) sync(history);
    }

    private void remember(History history) {
        if (history.getPosition() <= 0 || history.getDuration() <= 0) return;
        episodeProgress.put(key(history), new long[]{history.getPosition(), history.getDuration()});
    }

    private void restore(History history, Flag flag, Episode episode) {
        long[] saved = episodeProgress.get(key(history.getKey(), flag.getFlag(), episode));
        if (saved == null) {
            history.setPosition(C.TIME_UNSET);
            history.setDuration(C.TIME_UNSET);
            return;
        }
        history.setPosition(saved[0]);
        history.setDuration(saved[1]);
    }

    private boolean sameEpisode(History history, Flag flag, Episode episode) {
        if (!TextUtils.equals(history.getVodFlag(), flag.getFlag())) return false;
        return matchesCurrent(history, episode);
    }

    private boolean matchesCurrent(History history, Episode episode) {
        if (!episode.matchesName(history.getEpisode())) return false;
        String currentUrl = history.getEpisodeUrl();
        String nextUrl = episode.getUrl();
        if (currentUrl.isEmpty()) return true;
        if (nextUrl.isEmpty()) return false;
        return currentUrl.equals(nextUrl);
    }

    private String key(History history) {
        return key(history.getKey(), history.getVodFlag(), history.getEpisode());
    }

    private String key(String historyKey, String flag, Episode episode) {
        String id = episode.getUrl().isEmpty() ? episode.getName() : episode.getUrl();
        return historyKey + '\u0000' + (flag == null ? "" : flag) + '\u0000' + id;
    }

    public long startPositionMs(History history) {
        return history == null ? C.TIME_UNSET : Math.max(history.getOpening(), history.getPosition());
    }
}
