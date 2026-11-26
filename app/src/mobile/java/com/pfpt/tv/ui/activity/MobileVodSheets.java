package com.pfpt.tv.ui.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaChapter;
import androidx.media3.common.MediaEdition;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Tracks;
import androidx.media3.ui.DefaultTrackNameProvider;
import androidx.media3.ui.SubtitleView;

import com.pfpt.tv.App;
import com.pfpt.tv.R;
import com.pfpt.tv.api.DanmakuApi;
import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.Danmaku;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.Parse;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Track;
import com.pfpt.tv.impl.ParseCallback;
import com.pfpt.tv.model.VideoViewModel;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.vod.session.DownloadResolveOutcome;
import com.pfpt.tv.playback.vod.session.VodDownloadResolver;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.playback.vod.VodPlaybackState;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.player.parse.ParseJob;
import com.pfpt.tv.player.util.PlayerHelper;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.setting.PlayerSetting;
import com.pfpt.tv.ui.vod.VodDanmakuPrefs;
import com.pfpt.tv.ui.vod.compose.EngineSelection;
import com.pfpt.tv.ui.vod.compose.VodChapterSheetKt;
import com.pfpt.tv.ui.vod.compose.VodChapterState;
import com.pfpt.tv.ui.vod.compose.VodControlConfig;
import com.pfpt.tv.ui.vod.compose.VodControlConfigEvent;
import com.pfpt.tv.ui.vod.compose.VodControlConfigIds;
import com.pfpt.tv.ui.vod.compose.VodControlConfigSheetKt;
import com.pfpt.tv.ui.vod.compose.VodControlMoreIds;
import com.pfpt.tv.ui.vod.compose.VodControlMoreVisibility;
import com.pfpt.tv.ui.vod.compose.VodDanmakuEvent;
import com.pfpt.tv.ui.vod.compose.VodDanmakuIds;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSearchEvent;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSearchIds;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSearchSheetKt;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSearchState;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSettingEvent;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSettingIds;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSheetKt;
import com.pfpt.tv.ui.vod.compose.VodDanmakuState;
import com.pfpt.tv.ui.vod.compose.VodDownloadConfirm;
import com.pfpt.tv.ui.vod.compose.VodEditionSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEditionState;
import com.pfpt.tv.ui.vod.compose.VodEngineEvent;
import com.pfpt.tv.ui.vod.compose.VodEngineIds;
import com.pfpt.tv.ui.vod.compose.VodEngineSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEngineState;
import com.pfpt.tv.ui.vod.compose.VodOffsetEvent;
import com.pfpt.tv.ui.vod.compose.VodOffsetIds;
import com.pfpt.tv.ui.vod.compose.VodOffsetSheetKt;
import com.pfpt.tv.ui.vod.compose.VodOffsetState;
import com.pfpt.tv.ui.vod.compose.VodSheetController;
import com.pfpt.tv.ui.vod.compose.VodSheetKind;
import com.pfpt.tv.ui.vod.compose.VodSubtitleIds;
import com.pfpt.tv.ui.vod.compose.VodTimerEvent;
import com.pfpt.tv.ui.vod.compose.VodTimerIds;
import com.pfpt.tv.ui.vod.compose.VodTimerState;
import com.pfpt.tv.ui.vod.compose.VodTrackActions;
import com.pfpt.tv.ui.vod.compose.VodTrackEvent;
import com.pfpt.tv.ui.vod.compose.VodTrackIds;
import com.pfpt.tv.ui.vod.compose.VodTrackSheetKt;
import com.pfpt.tv.ui.vod.compose.VodTrackState;
import com.pfpt.tv.ui.dialog.DownloadDialog;
import com.pfpt.tv.utils.FileChooser;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Task;
import com.pfpt.tv.utils.Timer;
import com.pfpt.tv.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Response;

final class MobileVodSheets {

    interface Host {
        PlayerManager player();

        int scale();

        String text(int resId);

        Resources resources();

        VodSheetController sheet();

        boolean useParse();

        String openingLabel();

        String endingLabel();

        void onSpeedChanged(float speed);

        void onParse(Parse item);

        void onScale(int index);

        void onDecode();

        void onRepeat();

        void onOpening();

        boolean onOpeningReset();

        void onEnding();

        boolean onEndingReset();

        void hideControl();

        ActivityResultLauncher<Intent> trackSubtitleLauncher();

        ActivityResultLauncher<Intent> danmakuFileLauncher();

        boolean debugVisible();

        void toggleDebug();

        void hideDebug();

        void chooseOther(String title);

        String playTitle();

        void ensureSheet();

        void playEpisode(int index);

        VideoViewModel model();

        VodSessionViewModel session();

        String nameText();

        String siteKey();

        int episodeCount();

        void syncCatalog();

        boolean alive();

        void share(CharSequence title);

        void replay();

        void refresh();

        void rememberSpeed();

        void pushProgress();

        void armHide();

        SubtitleView subtitleView();

        android.app.Activity activity();
    }

    private final Host host;
    private int trackSheetType;
    private ParseJob downloadParse;
    private int downloadSeq;
    private final List<Danmaku> danmakuSearchItems = new ArrayList<>();
    private final okhttp3.Callback danmakuSearchCallback = new okhttp3.Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            App.post(() -> failDanmakuSearch(e));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            try (Response res = response) {
                String body = res.body() == null ? "" : res.body().string();
                List<Danmaku> items = Danmaku.arrayFrom(body);
                if (items.isEmpty()) throw new Exception(ResUtil.getString(R.string.error_empty));
                App.post(() -> showDanmakuSearchResult(items));
            } catch (Exception e) {
                App.post(() -> failDanmakuSearch(e));
            }
        }
    };
    private final Timer.Callback timerSheetCallback = new Timer.Callback() {
        @Override
        public void onTick(long tick) {
            host.sheet().updateTimer(readTimerState());
        }

        @Override
        public void onFinish() {
            dismissTimerSheet();
        }
    };

    MobileVodSheets(Host host) {
        this.host = host;
    }

    void onSetting() {
        host.ensureSheet();
        PlayerManager player = host.player();
        boolean showText = player != null && (player.haveTrack(C.TRACK_TYPE_TEXT) || player.isVod());
        boolean showAudio = player != null && player.haveTrack(C.TRACK_TYPE_AUDIO);
        boolean showVideo = player != null && player.haveTrack(C.TRACK_TYPE_VIDEO);
        boolean showDanmaku = player != null && player.haveDanmaku();
        boolean showSpeed = player != null && player.isVod();
        String speedLabel = player != null ? player.getSpeedText() : "";
        String[] scales = ResUtil.getStringArray(R.array.select_scale);
        int scale = host.scale();
        String scaleLabel = scale >= 0 && scale < scales.length ? scales[scale] : host.text(R.string.play_scale);
        String decodeLabel = player != null ? player.getDecodeText() : host.text(R.string.play_decode);
        boolean repeatOn = player != null && player.isRepeatOne();
        host.sheet().showControlMore(new VodControlMoreVisibility(showText, showAudio, showVideo, showDanmaku, showSpeed, speedLabel, scaleLabel, decodeLabel, host.openingLabel(), host.endingLabel(), repeatOn));
    }

    void onAllSettings() {
        if (host.player() == null) return;
        host.ensureSheet();
        host.sheet().showControlConfig(readControlConfig());
    }

    private VodControlConfig readControlConfig() {
        List<String> names = new ArrayList<>();
        int parseIndex = -1;
        List<Parse> parses = VodConfig.get().getParses();
        for (int i = 0; i < parses.size(); i++) {
            Parse item = parses.get(i);
            names.add(item.getName() == null ? "" : item.getName());
            if (item.isSelected()) parseIndex = i;
        }
        PlayerManager player = host.player();
        boolean showText = player.haveTrack(C.TRACK_TYPE_TEXT) || player.isVod();
        boolean showAudio = player.haveTrack(C.TRACK_TYPE_AUDIO);
        boolean showVideo = player.haveTrack(C.TRACK_TYPE_VIDEO);
        return VodControlConfigSheetKt.sanitizeVodControlConfig(new VodControlConfig(player.getSpeed(), host.useParse(), names, parseIndex, Arrays.asList(ResUtil.getStringArray(R.array.select_scale)), host.scale(), PlaybackAction.getEngineText(player), player.getDecodeText(), host.openingLabel(), host.endingLabel(), player.haveEdition(), player.haveChapter(), showText, showAudio, showVideo, false, DanmakuSetting.isLoad(), player.isRepeatOne(), Timer.get().isRunning()));
    }

    void onSheetControlConfig(VodControlConfigEvent event) {
        int id = event.getId();
        if (id == VodControlConfigIds.TIMER) {
            showTimerDialog();
            return;
        }
        if (host.player() == null) return;
        if (id == VodControlConfigIds.SPEED) {
            host.onSpeedChanged(event.getSpeed());
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.PARSE) {
            List<Parse> parses = VodConfig.get().getParses();
            int index = event.getIndex();
            if (index < 0 || index >= parses.size()) return;
            host.onParse(parses.get(index));
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.SCALE) {
            String[] scales = ResUtil.getStringArray(R.array.select_scale);
            int index = event.getIndex();
            if (index < 0 || index >= scales.length) return;
            host.onScale(index);
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.DECODE) {
            host.onDecode();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.REPEAT) {
            host.onRepeat();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.OPENING) {
            host.onOpening();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.OPENING_RESET) {
            host.onOpeningReset();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.ENDING) {
            host.onEnding();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.ENDING_RESET) {
            host.onEndingReset();
            host.sheet().updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.PLAYER) {
            onChoose();
        } else if (id == VodControlConfigIds.EDITION) {
            onEdition();
        } else if (id == VodControlConfigIds.CHAPTER) {
            onChapter();
        } else if (id == VodControlConfigIds.TEXT) {
            onTrack(C.TRACK_TYPE_TEXT);
        } else if (id == VodControlConfigIds.AUDIO) {
            onTrack(C.TRACK_TYPE_AUDIO);
        } else if (id == VodControlConfigIds.VIDEO) {
            onTrack(C.TRACK_TYPE_VIDEO);
        } else if (id == VodControlConfigIds.DANMAKU) {
            onDanmaku();
        }
    }

    private void showTimerDialog() {
        host.ensureSheet();
        Timer.get().setCallback(timerSheetCallback);
        host.sheet().showTimer(readTimerState());
    }

    private VodTimerState readTimerState() {
        boolean running = Timer.get().isRunning();
        String label = "";
        if (running) {
            StringBuilder builder = new StringBuilder();
            Formatter formatter = new Formatter(builder, Locale.getDefault());
            label = Util.format(builder, formatter, Timer.get().getTick());
        }
        return new VodTimerState(running, label);
    }

    void onTimerSheetEvent(VodTimerEvent event) {
        int id = event.getId();
        if (id == VodTimerIds.DELAY) {
            Timer.get().delay();
            return;
        }
        if (id == VodTimerIds.CANCEL) {
            Timer.get().reset();
            dismissTimerSheet();
            return;
        }
        if (id != VodTimerIds.MINUTES) return;
        int minutes = event.getMinutes();
        if (minutes != 5 && minutes != 15 && minutes != 30 && minutes != 60) return;
        Timer.get().set(TimeUnit.MINUTES.toMillis(minutes));
        dismissTimerSheet();
    }

    void dismissTimerSheet() {
        Timer.get().setCallback(null);
        host.sheet().dismiss();
    }

    void onTrack(int type) {
        PlayerManager player = host.player();
        if (player == null) return;
        if (type != C.TRACK_TYPE_AUDIO && type != C.TRACK_TYPE_VIDEO && type != C.TRACK_TYPE_TEXT) return;
        String[] titles = ResUtil.getStringArray(R.array.select_track);
        int titleIndex = type - 1;
        if (titleIndex < 0 || titleIndex >= titles.length) return;
        trackSheetType = type;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showTrack(readTrackState(titles[titleIndex]));
    }

    private VodTrackState readTrackState(String title) {
        List<Track> tracks = currentTracks(trackSheetType);
        List<String> names = new ArrayList<>();
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            names.add(tracks.get(i).getName() == null ? "" : tracks.get(i).getName());
            if (tracks.get(i).isSelected()) selected.add(i);
        }
        PlayerManager player = host.player();
        VodTrackActions actions = VodTrackSheetKt.vodTrackActions(trackSheetType, player.isVod(), player.haveTrack(trackSheetType));
        return new VodTrackState(title, names, selected, actions.getShowChoose(), actions.getShowSubtitle(), actions.getShowOffset());
    }

    private List<Track> currentTracks(int type) {
        List<Track> items = new ArrayList<>();
        DefaultTrackNameProvider provider = new DefaultTrackNameProvider(host.resources());
        List<Tracks.Group> groups = host.player().getCurrentTracks().getGroups();
        for (int i = 0; i < groups.size(); i++) {
            Tracks.Group group = groups.get(i);
            if (group.getType() != type) continue;
            for (int j = 0; j < group.length; j++) {
                Format format = group.getTrackFormat(j);
                Track item = new Track(type, provider.getTrackName(format), PlayerHelper.describeFormat(format));
                item.setSelected(group.isTrackSelected(j));
                items.add(item);
            }
        }
        return items;
    }

    void onTrackSheetEvent(VodTrackEvent event) {
        PlayerManager player = host.player();
        if (player == null) return;
        int id = event.getId();
        if (id == VodTrackIds.CHOOSE) {
            player.pause();
            FileChooser.from(host.trackSubtitleLauncher()).show(new String[]{MimeTypes.APPLICATION_SUBRIP, MimeTypes.TEXT_SSA, MimeTypes.TEXT_VTT, MimeTypes.APPLICATION_TTML, "audio/*", "text/*", "application/octet-stream"});
            return;
        }
        if (id == VodTrackIds.SUBTITLE) {
            onSubtitleClick();
            return;
        }
        if (id == VodTrackIds.OFFSET) {
            showOffsetSheet();
            return;
        }
        if (id != VodTrackIds.TRACK) return;
        List<Track> tracks = currentTracks(trackSheetType);
        int index = event.getIndex();
        if (index < 0 || index >= tracks.size()) return;
        player.setTrack(Arrays.asList(tracks.get(index).key(player.getKey()).save()));
    }

    void onEdition() {
        if (host.player() == null) return;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showEdition(readEditionState());
    }

    private VodEditionState readEditionState() {
        List<MediaEdition> items = host.player().getCurrentMediaEditions();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                MediaEdition item = items.get(i);
                names.add(VodEditionSheetKt.editionRowLabel(item.label == null ? "" : item.label, item.durationUs));
                if (selected < 0 && item.selected) selected = i;
            }
        }
        return new VodEditionState(host.text(R.string.dialog_select_edition), names, selected);
    }

    void onEditionSheetEvent(int index) {
        PlayerManager player = host.player();
        if (player == null) return;
        List<MediaEdition> items = player.getCurrentMediaEditions();
        if (items == null || index < 0 || index >= items.size()) return;
        player.selectEdition(items.get(index));
    }

    void onChapter() {
        if (host.player() == null) return;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showChapter(readChapterState());
    }

    private VodChapterState readChapterState() {
        List<MediaChapter> items = host.player().getCurrentMediaChapters();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                MediaChapter item = items.get(i);
                names.add(VodChapterSheetKt.chapterRowLabel(item.label == null ? "" : item.label, item.timeUs));
                if (selected < 0 && item.selected) selected = i;
            }
        }
        return new VodChapterState(host.text(R.string.dialog_select_chapter), names, selected);
    }

    void onChapterSheetEvent(int index) {
        PlayerManager player = host.player();
        if (player == null) return;
        List<MediaChapter> items = player.getCurrentMediaChapters();
        if (items == null || index < 0 || index >= items.size()) return;
        player.selectChapter(items.get(index));
    }

    void onDanmaku() {
        if (host.player() == null) return;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showDanmaku(readDanmakuState());
    }

    private VodDanmakuState readDanmakuState() {
        PlayerManager player = host.player();
        List<Danmaku> items = player.getDanmakus();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Danmaku item = items.get(i);
                names.add(item.getName() == null ? "" : item.getName());
                if (selected < 0 && item.isSelected()) selected = i;
            }
        }
        boolean showSearch = VodDanmakuSheetKt.danmakuShowSearch(player.getMetadata() != null, DanmakuSetting.getEffectiveApiUrl());
        return new VodDanmakuState(host.text(R.string.danmaku_select), names, selected, showSearch);
    }

    void onDanmakuSheetEvent(VodDanmakuEvent event) {
        PlayerManager player = host.player();
        if (player == null) return;
        int id = event.getId();
        if (id == VodDanmakuIds.CHOOSE) {
            player.pause();
            FileChooser.from(host.danmakuFileLauncher()).show("text/*");
            return;
        }
        if (id == VodDanmakuIds.SEARCH) {
            showDanmakuSearch();
            return;
        }
        if (id == VodDanmakuIds.SETTING) {
            showDanmakuSetting();
            return;
        }
        if (id != VodDanmakuIds.ITEM) return;
        List<Danmaku> items = player.getDanmakus();
        int index = event.getIndex();
        if (items == null || index < 0 || index >= items.size()) return;
        Danmaku item = items.get(index);
        player.setDanmaku(item.isSelected() ? Danmaku.empty() : item);
    }

    private void showDanmakuSearch() {
        PlayerManager player = host.player();
        if (player == null || player.getMetadata() == null) return;
        host.ensureSheet();
        danmakuSearchItems.clear();
        MediaMetadata metadata = player.getMetadata();
        String title = metadata.title == null ? "" : metadata.title.toString();
        host.sheet().showDanmakuSearch(new VodDanmakuSearchState(title, false, List.of(), -1));
    }

    void onDanmakuSearchEvent(VodDanmakuSearchEvent event) {
        PlayerManager player = host.player();
        if (player == null) return;
        if (event.getId() == VodDanmakuSearchIds.ITEM) {
            int index = event.getIndex();
            if (index < 0 || index >= danmakuSearchItems.size()) return;
            Danmaku item = danmakuSearchItems.get(index);
            player.setDanmaku(item.isSelected() ? Danmaku.empty() : item);
            return;
        }
        if (event.getId() != VodDanmakuSearchIds.SEARCH) return;
        if (!VodDanmakuSearchSheetKt.danmakuSearchReady(event.getKeyword())) return;
        MediaMetadata metadata = player.getMetadata();
        if (metadata == null) return;
        String episode = metadata.artist == null ? "" : metadata.artist.toString().trim();
        danmakuSearchItems.clear();
        host.sheet().updateDanmakuSearch(new VodDanmakuSearchState(host.sheet().getDanmakuSearch().getKeyword(), true, List.of(), -1));
        DanmakuApi.newCall(event.getKeyword().trim(), episode).enqueue(danmakuSearchCallback);
    }

    private void showDanmakuSearchResult(List<Danmaku> items) {
        if (host.sheet().getKind() != VodSheetKind.DanmakuSearch) return;
        danmakuSearchItems.clear();
        danmakuSearchItems.addAll(items);
        host.sheet().updateDanmakuSearch(readDanmakuSearchState(false));
    }

    private void failDanmakuSearch(Exception e) {
        if (host.sheet().getKind() != VodSheetKind.DanmakuSearch) return;
        host.sheet().updateDanmakuSearch(readDanmakuSearchState(false));
        Notify.show(e.getMessage());
    }

    private VodDanmakuSearchState readDanmakuSearchState(boolean loading) {
        List<String> names = new ArrayList<>();
        int selected = -1;
        for (int i = 0; i < danmakuSearchItems.size(); i++) {
            Danmaku item = danmakuSearchItems.get(i);
            names.add(item.getName() == null ? "" : item.getName());
            if (selected < 0 && item.isSelected()) selected = i;
        }
        return new VodDanmakuSearchState(host.sheet().getDanmakuSearch().getKeyword(), loading, names, selected);
    }

    void dismissDanmakuSearch() {
        DanmakuApi.cancel();
        if (host.sheet().getKind() == VodSheetKind.DanmakuSearch) host.sheet().dismiss();
    }

    private void showDanmakuSetting() {
        if (host.player() == null) return;
        host.ensureSheet();
        host.sheet().showDanmakuSetting(VodDanmakuPrefs.read(0));
    }

    void onDanmakuSettingEvent(VodDanmakuSettingEvent event) {
        int id = event.getId();
        int tab = event.getTab();
        if (id == VodDanmakuSettingIds.TAB) {
            if (host.sheet().getKind() != VodSheetKind.DanmakuSetting) return;
            host.sheet().updateDanmakuSetting(VodDanmakuPrefs.read(tab));
            return;
        }
        PlayerManager player = host.player();
        if (player == null) return;
        if (id == VodDanmakuSettingIds.RESET) {
            if (tab == 0) DanmakuSetting.resetAppearance();
            else if (tab == 1) DanmakuSetting.resetTiming();
            else if (tab == 2) DanmakuSetting.resetDensity();
            else if (tab == 3) DanmakuSetting.resetDisplay();
            else return;
        } else if (id == VodDanmakuSettingIds.BOOL) {
            if (!VodDanmakuPrefs.writeBool(event.getKey(), event.getBoolValue())) return;
        } else if (id == VodDanmakuSettingIds.FLOAT) {
            if (!VodDanmakuPrefs.writeFloat(event.getKey(), event.getFloatValue())) return;
        } else if (id == VodDanmakuSettingIds.MODE) {
            if (!VodDanmakuPrefs.writeMode(event.getKey(), event.getIntValue())) return;
        } else {
            return;
        }
        player.setDanmakuConfig(DanmakuSetting.getConfig());
        host.sheet().updateDanmakuSetting(VodDanmakuPrefs.read(tab));
    }

    void onChoose() {
        if (host.player() == null) return;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showEngine(readEngineState());
    }

    private VodEngineState readEngineState() {
        EngineSelection selection = VodEngineSheetKt.engineSelection(PlaybackAction.getEngine(host.player()));
        return new VodEngineState(host.text(R.string.player_engine), selection.getExo(), selection.getMpv(), host.debugVisible());
    }

    void onEngineSheetEvent(VodEngineEvent event) {
        PlayerManager player = host.player();
        if (player == null) return;
        int id = event.getId();
        if (id == VodEngineIds.DEBUG) {
            host.toggleDebug();
            return;
        }
        if (id == VodEngineIds.OTHER) {
            host.chooseOther(host.playTitle());
            return;
        }
        int engine = id == VodEngineIds.EXO ? PlayerSetting.ENGINE_EXO : id == VodEngineIds.MPV ? PlayerSetting.ENGINE_MPV : -1;
        if (engine != PlayerSetting.ENGINE_EXO && engine != PlayerSetting.ENGINE_MPV) return;
        if (engine != PlaybackAction.getEngine(player)) host.hideDebug();
        player.setEngine(engine);
    }

    void onSubtitleClick() {
        if (host.subtitleView() == null) return;
        host.hideControl();
        host.ensureSheet();
        host.sheet().showSubtitle();
    }

    void onSubtitleSheetEvent(int id) {
        SubtitleView view = host.subtitleView();
        if (view == null) return;
        if (id == VodSubtitleIds.LARGE) {
            view.addTextSize(0.002f);
            PlayerSetting.putSubtitleTextSize(view.getTextSize());
        } else if (id == VodSubtitleIds.SMALL) {
            view.subTextSize(0.002f);
            PlayerSetting.putSubtitleTextSize(view.getTextSize());
        } else if (id == VodSubtitleIds.UP) {
            view.addPosition(0.005f);
            PlayerSetting.putSubtitlePosition(view.getPosition());
        } else if (id == VodSubtitleIds.DOWN) {
            view.subPosition(0.005f);
            PlayerSetting.putSubtitlePosition(view.getPosition());
        } else if (id == VodSubtitleIds.RESET) {
            PlayerSetting.putSubtitleTextSize(0.0f);
            PlayerSetting.putSubtitlePosition(0.0f);
            view.reset();
        } else {
            return;
        }
        PlayerManager player = host.player();
        if (player != null && !player.isReleased()) player.setSubtitleStyle();
    }

    private void showOffsetSheet() {
        if (host.player() == null) return;
        if (trackSheetType != C.TRACK_TYPE_AUDIO && trackSheetType != C.TRACK_TYPE_TEXT) return;
        host.ensureSheet();
        host.sheet().showOffset(readOffsetState());
    }

    private VodOffsetState readOffsetState() {
        boolean audio = trackSheetType == C.TRACK_TYPE_AUDIO;
        float from = audio ? -10000f : -300000f;
        float to = audio ? 10000f : 300000f;
        float step = audio ? 100f : 1000f;
        long raw = audio ? host.player().getAudioOffsetMs() : host.player().getTextOffsetMs();
        float value = VodOffsetSheetKt.snapOffset(raw, from, to, step);
        return new VodOffsetState(host.text(R.string.offset_setting), audio, value);
    }

    void onOffsetSheetEvent(VodOffsetEvent event) {
        PlayerManager player = host.player();
        if (player == null) return;
        boolean audio = trackSheetType == C.TRACK_TYPE_AUDIO;
        if (event.getId() == VodOffsetIds.RESET) {
            if (audio) player.setAudioOffsetMs(0);
            else player.setTextOffsetMs(0);
            host.sheet().updateOffset(readOffsetState());
            return;
        }
        if (event.getId() != VodOffsetIds.CHANGE) return;
        float from = audio ? -10000f : -300000f;
        float to = audio ? 10000f : 300000f;
        float step = audio ? 100f : 1000f;
        float value = VodOffsetSheetKt.snapOffset(event.getValueMs(), from, to, step);
        long ms = Math.round(value);
        if (audio) player.setAudioOffsetMs(ms);
        else player.setTextOffsetMs(ms);
        host.sheet().updateOffset(new VodOffsetState(host.text(R.string.offset_setting), audio, value));
    }

    void onDownload() {
        if (host.episodeCount() == 0) return;
        host.syncCatalog();
        host.ensureSheet();
        host.sheet().showEpisodes(true);
    }

    void onSheetPlayEpisode(int index) {
        host.playEpisode(index);
    }

    void onSheetDownloadEpisode(int index) {
        VodPlaybackState state = host.model().getPlaybackState();
        if (!state.hasFlags()) return;
        List<Episode> episodes = state.getFlag().getEpisodes();
        if (index < 0 || index >= episodes.size()) return;
        onDownloadEpisode(episodes.get(index));
    }

    void onSheetDownloadList() {
        DownloadDialog.openList(host.activity());
    }

    void onSheetDownloadConfirm(VodDownloadConfirm confirm) {
        if (DownloadDialog.send(host.activity(), confirm.getFileName(), confirm.getUrl(), confirm.getHeaders())) {
            VodSessionViewModel session = host.session();
            if (session != null) session.clearDownloadReady();
            host.sheet().dismiss();
        }
    }

    void onSheetInfoShare(String title) {
        host.share(title);
    }

    void onSheetControlMore(int id) {
        if (id == VodControlMoreIds.EPISODES) {
            host.syncCatalog();
            host.ensureSheet();
            host.sheet().showEpisodes(false);
        } else if (id == VodControlMoreIds.SPEED) {
            onCycleSpeed();
        } else if (id == VodControlMoreIds.SCALE) {
            host.onScale((host.scale() + 1) % 5);
        } else if (id == VodControlMoreIds.DECODE) {
            host.onDecode();
        } else if (id == VodControlMoreIds.REPEAT) {
            host.onRepeat();
        } else if (id == VodControlMoreIds.OPENING) {
            host.onOpening();
        } else if (id == VodControlMoreIds.ENDING) {
            host.onEnding();
        } else if (id == VodControlMoreIds.INFO) {
            onPlaybackInfo();
        } else if (id == VodControlMoreIds.TEXT) onTrack(C.TRACK_TYPE_TEXT);
        else if (id == VodControlMoreIds.SUBTITLE) onSubtitleClick();
        else if (id == VodControlMoreIds.AUDIO) onTrack(C.TRACK_TYPE_AUDIO);
        else if (id == VodControlMoreIds.VIDEO) onTrack(C.TRACK_TYPE_VIDEO);
        else if (id == VodControlMoreIds.DANMAKU) onDanmaku();
        else if (id == VodControlMoreIds.REPLAY) host.replay();
        else if (id == VodControlMoreIds.RESET) host.refresh();
        else if (id == VodControlMoreIds.ALL_SETTINGS) onAllSettings();
    }

    private void onCycleSpeed() {
        PlayerManager player = host.player();
        if (player == null) return;
        player.addSpeed();
        host.rememberSpeed();
        host.pushProgress();
        host.armHide();
    }

    private void onPlaybackInfo() {
        PlayerManager player = host.player();
        host.ensureSheet();
        host.sheet().showInfo(host.nameText(), player.getUrl(), player.getHeaders());
    }

    private void onDownloadEpisode(Episode item) {
        int seq = ++downloadSeq;
        stopDownloadParse();
        VodPlaybackState state = host.model().getPlaybackState();
        Episode activated = state.hasEpisode() ? state.getEpisode() : null;
        if (item.equals(activated)) {
            DownloadResolveOutcome.Ready ready = host.session().prepareDownloadFromPlayer(host.player().getUrl(), host.player().getHeaders(), VodDownloadResolver.buildFileName(host.nameText(), item.getName()));
            if (ready != null) {
                showDownload(item, ready.getUrl(), ready.getHeaders());
                return;
            }
        }
        Flag flag = state.hasFlags() ? state.getFlag() : null;
        if (flag == null) {
            Notify.show(R.string.error_play_flag);
            return;
        }
        Notify.show(R.string.download_fetch);
        String key = host.siteKey();
        String flagName = flag.getFlag();
        String id = item.getUrl();
        Task.execute(() -> {
            try {
                Result result = SiteApi.playerContent(key, flagName, id, false);
                App.post(() -> onDownloadContent(seq, item, result));
            } catch (Exception e) {
                App.post(() -> onDownloadFailed(seq, R.string.error_play_url));
            }
        });
    }

    private void onDownloadContent(int seq, Episode item, Result result) {
        if (!acceptDownload(seq)) return;
        DownloadResolveOutcome outcome = host.session().resolveDownloadResult(result);
        if (outcome instanceof DownloadResolveOutcome.Message message) {
            Notify.show(message.getText());
            return;
        }
        if (outcome instanceof DownloadResolveOutcome.EmptyUrl) {
            Notify.show(R.string.error_play_url);
            return;
        }
        if (outcome instanceof DownloadResolveOutcome.NeedParse needParse) {
            Result parseResult = needParse.getResult();
            downloadParse = ParseJob.create(new ParseCallback() {
                @Override
                public void onParseSuccess(Map<String, String> headers, String url, String from) {
                    if (!acceptDownload(seq)) return;
                    DownloadResolveOutcome.Ready ready = host.session().prepareDownloadFromPlayer(url, headers, VodDownloadResolver.buildFileName(host.nameText(), item.getName()));
                    if (ready == null) Notify.show(R.string.error_play_parse);
                    else showDownload(item, ready.getUrl(), ready.getHeaders());
                }

                @Override
                public void onParseError() {
                    onDownloadFailed(seq, R.string.error_play_parse);
                }
            }).start(parseResult, parseResult.isUseParse());
            return;
        }
        if (outcome instanceof DownloadResolveOutcome.Ready ready) {
            showDownload(item, ready.getUrl(), ready.getHeaders());
        }
    }

    private void onDownloadFailed(int seq, int resId) {
        if (!acceptDownload(seq)) return;
        Notify.show(resId);
    }

    private boolean acceptDownload(int seq) {
        return seq == downloadSeq && host.alive();
    }

    void stopDownloadParse() {
        if (downloadParse == null) return;
        downloadParse.stop();
        downloadParse = null;
    }

    private void showDownload(Episode item, String url, Map<String, String> headers) {
        if (TextUtils.isEmpty(url)) return;
        String save = VodDownloadResolver.buildFileName(host.nameText(), item == null ? "" : item.getName());
        VodPlaybackState state = host.model().getPlaybackState();
        Episode activated = state.hasEpisode() ? state.getEpisode() : null;
        long duration = item != null && item.equals(activated) ? host.player().getDuration() : 0;
        host.session().presentDownloadConfirm(save, url, duration, headers);
        host.ensureSheet();
        host.sheet().showDownloadConfirm(save, url, duration, headers);
    }
}
