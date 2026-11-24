package com.pfpt.tv.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaChapter;
import androidx.media3.common.MediaEdition;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.DefaultTrackNameProvider;
import androidx.media3.ui.PlayerSeekView;
import androidx.media3.ui.PlayerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.pfpt.tv.App;
import com.pfpt.tv.Constant;
import com.pfpt.tv.R;
import com.pfpt.tv.api.DanmakuApi;
import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.CastVideo;
import com.pfpt.tv.bean.Danmaku;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Keep;
import com.pfpt.tv.bean.Parse;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Sub;
import com.pfpt.tv.bean.Track;
import com.pfpt.tv.bean.Url;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.databinding.ActivityVideoBinding;
import com.pfpt.tv.event.CastEvent;
import com.pfpt.tv.event.RefreshEvent;
import com.pfpt.tv.impl.CustomTarget;
import com.pfpt.tv.impl.ParseCallback;
import com.pfpt.tv.model.VideoViewModel;
import com.pfpt.tv.player.parse.ParseJob;
import com.pfpt.tv.player.util.PlayerHelper;
import com.pfpt.tv.service.PlaybackService;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.setting.LiveSetting;
import com.pfpt.tv.setting.PlayerSetting;
import com.pfpt.tv.ui.custom.CustomKeyDown;
import com.pfpt.tv.ui.dialog.CastDialog;
import com.pfpt.tv.ui.dialog.DanmakuSearchDialog;
import com.pfpt.tv.ui.dialog.DanmakuSettingDialog;
import com.pfpt.tv.ui.dialog.DownloadDialog;
import com.pfpt.tv.ui.dialog.ParseDialog;
import com.pfpt.tv.ui.dialog.ReceiveDialog;
import com.pfpt.tv.ui.dialog.SubtitleDialog;
import com.pfpt.tv.ui.dialog.OffsetDialog;
import com.pfpt.tv.ui.dialog.TrackDialog;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.PlaybackOrientation;
import com.pfpt.tv.playback.vod.VodPlaybackActivityAccess;
import com.pfpt.tv.playback.vod.VodPlaybackController;
import com.pfpt.tv.playback.vod.VodPlaybackMedia;
import com.pfpt.tv.playback.vod.VodPlaybackState;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.playback.vod.session.DownloadResolveOutcome;
import com.pfpt.tv.playback.vod.session.VodDownloadResolver;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.ui.vod.compose.VodComposeHostKt;
import com.pfpt.tv.ui.vod.compose.VodControlConfig;
import com.pfpt.tv.ui.vod.compose.VodControlConfigEvent;
import com.pfpt.tv.ui.vod.compose.VodControlConfigIds;
import com.pfpt.tv.ui.vod.compose.VodControlConfigSheetKt;
import com.pfpt.tv.ui.vod.compose.VodControlMoreIds;
import com.pfpt.tv.ui.vod.compose.VodControlMoreVisibility;
import com.pfpt.tv.ui.vod.compose.VodDownloadConfirm;
import com.pfpt.tv.ui.vod.compose.VodPlayerScale;
import com.pfpt.tv.ui.vod.compose.VodSheetController;
import com.pfpt.tv.ui.vod.compose.VodSheetsKt;
import com.pfpt.tv.ui.vod.compose.EngineSelection;
import com.pfpt.tv.ui.vod.compose.VodChapterSheetKt;
import com.pfpt.tv.ui.vod.compose.VodChapterState;
import com.pfpt.tv.ui.vod.compose.VodDanmakuEvent;
import com.pfpt.tv.ui.vod.compose.VodDanmakuIds;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSheetKt;
import com.pfpt.tv.ui.vod.compose.VodDanmakuState;
import com.pfpt.tv.ui.vod.compose.VodEditionSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEditionState;
import com.pfpt.tv.ui.vod.compose.VodEngineEvent;
import com.pfpt.tv.ui.vod.compose.VodEngineIds;
import com.pfpt.tv.ui.vod.compose.VodEngineSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEngineState;
import com.pfpt.tv.ui.vod.compose.VodTimerEvent;
import com.pfpt.tv.ui.vod.compose.VodTimerIds;
import com.pfpt.tv.ui.vod.compose.VodTimerState;
import com.pfpt.tv.ui.vod.compose.VodTrackActions;
import com.pfpt.tv.ui.vod.compose.VodTrackEvent;
import com.pfpt.tv.ui.vod.compose.VodTrackIds;
import com.pfpt.tv.ui.vod.compose.VodTrackSheetKt;
import com.pfpt.tv.ui.vod.compose.VodTrackState;
import com.pfpt.tv.utils.Clock;
import com.pfpt.tv.utils.FileChooser;
import com.pfpt.tv.utils.ImgUtil;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.PiP;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Sniffer;
import com.pfpt.tv.utils.Task;
import com.pfpt.tv.utils.Timer;
import com.pfpt.tv.utils.Traffic;
import com.pfpt.tv.utils.UrlUtil;
import com.pfpt.tv.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends PlaybackActivity implements VodPlaybackActivityAccess, Clock.Callback, CustomKeyDown.Listener, TrackDialog.Listener, ParseDialog.Listener, CastDialog.Listener {

    ActivityVideoBinding mBinding;
    private ViewGroup.LayoutParams mFrameParams;
    private Observer<Result> mObserveDetail;
    private Observer<Result> mObservePlayer;
    private Observer<Result> mObserveSearch;
    private ParseJob downloadParse;
    private int downloadSeq;
    private VodPlaybackController mVod;
    VideoViewModel mViewModel;
    private VodSessionViewModel mSession;
    private String siteRawForCompose;
    private String remarkRawForCompose;
    private ValueAnimator mAnimator;
    private CustomKeyDown mKeyDown;
    private VodPlayerScale mPlayerScale;
    History mHistory;
    private boolean fullscreen;
    private boolean useParse;
    private boolean rotate;
    private String playTitle = "";
    private final VodSheetController sheetController = new VodSheetController();
    private int trackSheetType;
    private final ActivityResultLauncher<Intent> trackSubtitleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        if (player() == null) return;
        player().setSub(Sub.from(FileChooser.getPathFromUri(result.getData().getData())));
        sheetController.dismiss();
    });
    private final ActivityResultLauncher<Intent> danmakuFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        if (player() == null) return;
        player().setDanmaku(Danmaku.from(FileChooser.getPathFromUri(result.getData().getData())));
        sheetController.dismiss();
    });
    private final Timer.Callback timerSheetCallback = new Timer.Callback() {
        @Override
        public void onTick(long tick) {
            sheetController.updateTimer(readTimerState());
        }

        @Override
        public void onFinish() {
            dismissTimerSheet();
        }
    };
    private boolean gestureBrightArmed;
    private float gestureBright;
    private boolean gestureVolumeArmed;
    private float gestureVolume;
    private int gestureVolumeMax;
    private boolean gestureSeeking;
    private long gestureSeekMs;
    private boolean gestureAnimating;
    private AudioManager gestureAudio;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    Runnable mR4;
    Clock mProgressClock;
    private PiP mPiP;

    public static void push(FragmentActivity activity, String text) {
        Uri uri = UrlUtil.uri(text);
        if (FileChooser.isValid(activity, uri)) file(activity, FileChooser.getPathFromUri(uri));
        else start(activity, Sniffer.getUrl(text));
    }

    public static void file(FragmentActivity activity, String path) {
        if (TextUtils.isEmpty(path)) return;
        String name = new File(path).getName();
        start(activity, SiteApi.PUSH, "file://" + path, name);
    }

    public static void cast(Activity activity, History history) {
        start(activity, history.getSiteKey(), history.getVodId(), history.getVodName(), history.getVodPic());
    }

    public static void collect(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null, true);
    }

    public static void start(Activity activity, String url) {
        start(activity, SiteApi.PUSH, url, url);
    }

    public static void start(Activity activity, String key, String id, String name) {
        start(activity, key, id, name, null);
    }

    public static void start(Activity activity, String key, String id, String name, String pic) {
        start(activity, key, id, name, pic, null);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark) {
        start(activity, key, id, name, pic, mark, false);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean collect) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("collect", collect);
        intent.putExtra("mark", mark);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("key", key);
        intent.putExtra("id", id);
        activity.startActivity(intent);
    }

    @Override
    public String getName() {
        return Objects.toString(getIntent().getStringExtra("name"), "");
    }

    @Override
    public String getPic() {
        return Objects.toString(getIntent().getStringExtra("pic"), "");
    }

    @Override
    public String getMark() {
        return Objects.toString(getIntent().getStringExtra("mark"), "");
    }

    @Override
    public String getKey() {
        return Objects.toString(getIntent().getStringExtra("key"), "");
    }

    @Override
    public String getId() {
        return Objects.toString(getIntent().getStringExtra("id"), "");
    }

    @Override
    public String getHistoryKey() {
        return History.key(getKey(), getId());
    }

    @Override
    public Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    @Override
    public Context getHostContext() {
        return this;
    }

    @Override
    public String getNameText() {
        if (mSession == null) return "";
        return mSession.getUiState().getValue().getTitle();
    }

    @Override
    public PlayerManager player() {
        return super.player();
    }

    @Override
    public void startPlayer(String key, Result result, boolean useParse, long timeout, long startPositionMs, MediaMetadata metadata) {
        super.startPlayer(key, result, useParse, timeout, startPositionMs, metadata);
    }

    @Override
    public void detailContent(String key, String id) {
        mViewModel.detailContent(key, id);
    }

    @Override
    public void searchContent(List<Site> sites, String keyword, boolean quick) {
        mViewModel.searchContent(sites, keyword, quick);
    }

    @Override
    public void clearQuickAdapter() {
        if (mSession != null) mSession.clearQuick();
    }

    @Override
    public void addQuickItems(List<Vod> items) {
        if (mSession != null) mSession.addQuick(items);
    }

    @Override
    public boolean isQuickAdapterEmpty() {
        return mSession == null || mSession.isQuickEmpty();
    }

    @Override
    public void setQuickVisible(int visibility) {
    }

    @Override
    public void clearClockCallback() {
        mProgressClock.setCallback(null);
    }

    @Override
    public void postFallbackRunnable(long delayMs) {
        App.post(mR4, delayMs);
    }

    @Override
    public void removeFallbackRunnable() {
        App.removeCallbacks(mR4);
    }

    private Episode getEpisode() {
        VodPlaybackState state = mViewModel.getPlaybackState();
        return state.hasEpisode() ? state.getEpisode() : new Episode();
    }

    public int getScale() {
        return History.resolveScale(mHistory);
    }

    void setPlayTitle(CharSequence title) {
        playTitle = title == null ? "" : title.toString();
    }

    void setScale(int scale) {
        mVod.setScale(scale);
        mBinding.player.setResizeMode(scale);
    }

    private boolean isAutoRotate() {
        return Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
    }

    private boolean isLand() {
        return mBinding.getRoot().getTag().equals("land");
    }

    private boolean isPort() {
        return mBinding.getRoot().getTag().equals("port");
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityVideoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected PlaybackService.NavigationCallback getNavigationCallback() {
        return mNavigationCallback;
    }

    @Override
    protected PlayerView getPlayerView() {
        return mBinding.player;
    }

    @Override
    protected PlayerSeekView getSeekView() {
        return null;
    }

    @Override
    protected void onServiceConnected() {
        checkLand();
        checkId();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String oldId = getId();
        super.onNewIntent(intent);
        String id = Objects.toString(intent.getStringExtra("id"), "");
        if (TextUtils.isEmpty(id) || id.equals(oldId)) return;
        mBinding.swipeLayout.setRefreshing(true);
        saveHistory(false);
        getIntent().putExtras(intent);
        mVod.reset();
        setOrient();
        checkId();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> setStatusBar(insets));
        mKeyDown = CustomKeyDown.create(this, mBinding.player);
        mPlayerScale = new VodPlayerScale(mBinding.player);
        mFrameParams = mBinding.video.getLayoutParams();
        mBinding.progressLayout.showProgress();
        mBinding.swipeLayout.setEnabled(false);
        mObserveDetail = this::onDetailObserved;
        mObservePlayer = this::onPlayerObserved;
        mObserveSearch = this::onSearchObserved;
        mProgressClock = Clock.create().format("HH:mm");
        mR1 = this::hideControl;
        mR2 = this::setTraffic;
        mR3 = this::setOrient;
        mR4 = this::showEmpty;
        mPiP = new PiP();
        checkDanmakuImg();
        setVideoView();
        setViewModel();
        showProgress();
        setAnimator();
    }

    @Override
    protected void initEvent() {
        mBinding.swipeLayout.setOnRefreshListener(this::onSwipeRefresh);
    }

    private WindowInsetsCompat setStatusBar(WindowInsetsCompat insets) {
        int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        ViewGroup.LayoutParams lp = mBinding.statusBar.getLayoutParams();
        lp.height = top;
        mBinding.statusBar.setLayoutParams(lp);
        return insets;
    }

    private void setVideoView() {
        mBinding.video.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> mPiP.update(this, view));
    }

    private void setVideoView(boolean isInPictureInPictureMode) {
        if (isInPictureInPictureMode) {
            mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            mBinding.video.setLayoutParams(mFrameParams);
        }
    }

    private void setAnimator() {
        mAnimator = new ValueAnimator();
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            if (isLand() || isFullscreen() || isInPictureInPictureMode()) return;
            mFrameParams.height = (int) animation.getAnimatedValue();
            mBinding.video.setLayoutParams(mFrameParams);
        });
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        mSession = new ViewModelProvider(this).get(VodSessionViewModel.class);
        mSession.bind(mViewModel.getPlaybackState());
        if (mBinding.catalogCompose != null) {
            VodComposeHostKt.setVodDetailHeader(mBinding.detailCompose, mSession, this::onKeep, this::onName, () -> onChange(), this::onComposeSiteClick, this::onComposeRemarkClick, this::onDownload, this::onReverse, this::onMore, this::onCopy);
            VodComposeHostKt.setVodDetailCatalog(mBinding.catalogCompose, mSession, this::onComposeFlag, this::onComposeQuality, this::onComposeEpisode, this::onComposeQuick);
        } else {
            VodComposeHostKt.setVodScreen(mBinding.detailCompose, mSession, this::onKeep, this::onName, () -> onChange(), this::onComposeSiteClick, this::onComposeRemarkClick, this::onDownload, this::onReverse, this::onMore, this::onCopy, this::onComposeFlag, this::onComposeQuality, this::onComposeEpisode, this::onComposeQuick);
        }
        VodSheetsKt.setVodSheetHost(mBinding.sheetCompose, mSession, sheetController, this::onSheetPlayEpisode, this::onSheetDownloadEpisode, this::onSheetDownloadList, this::onSheetDownloadConfirm, this::onSheetInfoShare, this::onSheetControlMore, this::onSheetControlConfig, this::onTimerSheetEvent, this::dismissTimerSheet, this::onTrackSheetEvent, this::onEditionSheetEvent, this::onChapterSheetEvent, this::onEngineSheetEvent, this::onDanmakuSheetEvent);
        VodComposeHostKt.setVodControlBars(mBinding.controlTopCompose, mBinding.controlCenterCompose, mBinding.controlLockCompose, mBinding.controlBottomCompose, mSession, this::onBack, this::checkPlay, this::checkNext, this::checkPrev, this::onLock, this::onKeep, this::enterFullscreen, this::onSetting, this::onComposeSeek, this::onComposeScrub, this::onCast, this::onPip, this::onRotate, this::onDanmakuShow);
        VodComposeHostKt.setVodGestureLayer(mBinding.gestureCompose, mSession, this::onComposeBrightness, this::onComposeVolume, this::onComposeSeekPreview, this::onComposeSeekEnd, this::onSingleTap, this::onDoubleTap, this::onSpeedUp, this::onSpeedEnd, this::onComposeFlingUp, this::onComposeFlingDown, this::onComposeScale, this::onComposeTouchEnd);
        observeForever(mViewModel.getResult(), mObserveDetail);
        observeForever(mViewModel.getPlayer(), mObservePlayer);
        observeForever(mViewModel.getSearch(), mObserveSearch);
        mVod = mViewModel.createPlaybackController(new VodPlaybackBinder(this));
    }

    private void onDetailObserved(Result result) {
        if (service() == null) return;
        mVod.onDetailResult(result);
    }

    private void onPlayerObserved(Result result) {
        if (service() == null) return;
        mVod.onPlayerResult(result);
    }

    private void onSearchObserved(Result result) {
        if (service() == null) return;
        mVod.onSearchResult(result);
    }

    private void checkId() {
        mVod.checkId();
    }

    private void checkLand() {
        if (isPort() && ResUtil.isLand(this)) enterFullscreen();
    }

    @Override
    public void showEmpty() {
        showError(getString(R.string.error_detail));
        mBinding.swipeLayout.setEnabled(true);
        mBinding.progressLayout.showEmpty();
    }

    void setText(Vod item) {
        String siteRaw = getSite().getName();
        String remarkRaw = item.getRemarks();
        siteRawForCompose = siteRaw;
        remarkRawForCompose = remarkRaw;
        String siteText = TextUtils.isEmpty(siteRaw) ? null : getString(R.string.detail_site, siteRaw);
        String remarkText = TextUtils.isEmpty(remarkRaw) ? null : remarkRaw;
        String title = TextUtils.isEmpty(item.getName()) ? null : item.getName();
        updateDetailMeta(title, siteText, remarkText);
        if (mSession != null) {
            mSession.updateDetailBody(
                    item.getYear(),
                    item.getArea(),
                    item.getTypeName(),
                    item.getDirector(),
                    item.getActor(),
                    item.getContent());
        }
    }

    void setDescription(String desc) {
        if (mSession == null) return;
        if (TextUtils.isEmpty(desc) && !TextUtils.isEmpty(mSession.getUiState().getValue().getContent())) return;
        mSession.updateDetailBody(null, null, null, null, null, desc);
    }

    void updateDetailMeta(String title, String siteName, String remark) {
        if (mSession != null) mSession.updateDetailMeta(title, siteName, remark);
    }

    private void onComposeSiteClick() {
        openSniffedLinks(siteRawForCompose);
    }

    private void onComposeRemarkClick() {
        openSniffedLinks(remarkRawForCompose);
    }

    private void openSniffedLinks(String text) {
        if (TextUtils.isEmpty(text)) return;
        final Result[] link = new Result[1];
        Sniffer.buildClickable(text, result -> {
            if (link[0] == null) link[0] = result;
            return new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                }
            };
        });
        if (link[0] == null) return;
        FolderActivity.start(getActivity(), getKey(), link[0]);
        setRedirect(true);
    }

    private void onFlagClick(Flag item) {
        mVod.selectFlag(item);
    }

    private void onEpisodeClick(Episode item) {
        if (shouldEnterFullscreen(item)) return;
        mVod.selectEpisode(item);
    }

    private void onQualityClick(Result result) {
        mVod.selectQuality(result);
        syncCatalog();
    }

    private void onComposeFlag(int index) {
        List<Flag> flags = mViewModel.getPlaybackState().getFlags();
        if (index < 0 || index >= flags.size()) return;
        onFlagClick(flags.get(index));
    }

    private void onComposeEpisode(int index) {
        VodPlaybackState state = mViewModel.getPlaybackState();
        if (!state.hasFlags()) return;
        List<Episode> episodes = state.getFlag().getEpisodes();
        if (index < 0 || index >= episodes.size()) return;
        onEpisodeClick(episodes.get(index));
    }

    private void onComposeQuality(int index) {
        Result quality = mViewModel.getPlaybackState().getQuality();
        if (quality == null) return;
        Url url = quality.getUrl();
        if (url == null || index < 0 || index >= url.getValues().size()) return;
        url.set(index);
        onQualityClick(quality);
    }

    private void onComposeQuick(int index) {
        if (mSession == null) return;
        Vod item = mSession.quickAt(index);
        if (item != null) mVod.selectSource(item);
    }

    void syncCatalog() {
        if (mSession == null) return;
        mSession.refresh();
    }

    @Override
    public void setEpisodeAdapter(List<Episode> items) {
        syncCatalog();
    }

    @Override
    public void setQualityVisible(boolean visible) {
        syncCatalog();
    }

    private void onName() {
        String name = getNameText();
        if (name.isEmpty()) name = getName();
        Notify.show(getString(R.string.detail_search, name));
        mVod.search(name, false);
    }

    private void onMore() {
        syncCatalog();
        ensureSheetHost();
        sheetController.showEpisodes(false);
    }

    private void onReverse() {
        mVod.setRevSort(!mHistory.isRevSort());
        mVod.reverseEpisode(false);
    }

    private boolean onChange() {
        mVod.manualSwitchSource();
        return true;
    }

    private void onCopy() {
        Util.copy(mSession == null ? "" : mSession.getUiState().getValue().getContent());
    }

    private void onBack() {
        if (isFullscreen()) exitFullscreen();
        else finish();
    }

    private void onCast() {
        CastDialog.create().history(mHistory).video(new CastVideo(getNameText(), player().getUrl(), player().getPosition(), player().getHeaders())).fm(true).show(this);
    }

    private void onKeep() {
        if (mHistory == null || mSession == null) return;
        mSession.toggleKeep(getHistoryKey(), VodConfig.getCid(), mHistory.getVodName(), mHistory.getVodPic(), getSite().getName(), (kept, removed) -> {
            if (isFinishing() || isDestroyed()) return;
            Notify.show(removed ? R.string.keep_del : R.string.keep_add);
        });
    }

    private void checkPlay() {
        setR1Callback();
        if (player().isPlaying()) onPaused();
        else if (player().isEmpty()) onRefresh();
        else onPlay();
    }

    private void checkNext() {
        checkNext(true);
    }

    private void checkNext(boolean notify) {
        setR1Callback();
        mVod.nextEpisode(notify);
    }

    private void checkPrev() {
        setR1Callback();
        mVod.prevEpisode(true);
    }

    private void onSetting() {
        ensureSheetHost();
        boolean showText = player() != null && (player().haveTrack(C.TRACK_TYPE_TEXT) || player().isVod());
        boolean showAudio = player() != null && player().haveTrack(C.TRACK_TYPE_AUDIO);
        boolean showVideo = player() != null && player().haveTrack(C.TRACK_TYPE_VIDEO);
        boolean showDanmaku = player() != null && player().haveDanmaku();
        boolean showSpeed = player() != null && player().isVod();
        String speedLabel = player() != null ? player().getSpeedText() : "";
        String[] scales = ResUtil.getStringArray(R.array.select_scale);
        int scale = getScale();
        String scaleLabel = scale >= 0 && scale < scales.length ? scales[scale] : getString(R.string.play_scale);
        String decodeLabel = player() != null ? player().getDecodeText() : getString(R.string.play_decode);
        boolean repeatOn = player() != null && player().isRepeatOne();
        sheetController.showControlMore(new VodControlMoreVisibility(
                showText, showAudio, showVideo, showDanmaku, showSpeed, speedLabel,
                scaleLabel, decodeLabel, openingLabel(), endingLabel(), repeatOn));
    }

    private void onAllSettings() {
        if (player() == null) return;
        ensureSheetHost();
        sheetController.showControlConfig(readControlConfig());
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
        boolean showText = player().haveTrack(C.TRACK_TYPE_TEXT) || player().isVod();
        boolean showAudio = player().haveTrack(C.TRACK_TYPE_AUDIO);
        boolean showVideo = player().haveTrack(C.TRACK_TYPE_VIDEO);
        return VodControlConfigSheetKt.sanitizeVodControlConfig(new VodControlConfig(
                player().getSpeed(),
                isUseParse(),
                names,
                parseIndex,
                Arrays.asList(ResUtil.getStringArray(R.array.select_scale)),
                getScale(),
                PlaybackAction.getEngineText(player()),
                player().getDecodeText(),
                openingLabel(),
                endingLabel(),
                player().haveEdition(),
                player().haveChapter(),
                showText,
                showAudio,
                showVideo,
                false,
                DanmakuSetting.isLoad(),
                player().isRepeatOne(),
                Timer.get().isRunning()
        ));
    }

    private void onSheetControlConfig(VodControlConfigEvent event) {
        int id = event.getId();
        if (id == VodControlConfigIds.TIMER) {
            showTimerDialog();
            return;
        }
        if (player() == null) return;
        if (id == VodControlConfigIds.SPEED) {
            onSpeedChanged(event.getSpeed());
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.PARSE) {
            List<Parse> parses = VodConfig.get().getParses();
            int index = event.getIndex();
            if (index < 0 || index >= parses.size()) return;
            onParse(parses.get(index));
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.SCALE) {
            String[] scales = ResUtil.getStringArray(R.array.select_scale);
            int index = event.getIndex();
            if (index < 0 || index >= scales.length) return;
            onScale(index);
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.DECODE) {
            onDecode();
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.REPEAT) {
            onRepeat();
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.OPENING) {
            onOpening();
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.OPENING_RESET) {
            onOpeningReset();
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.ENDING) {
            onEnding();
            sheetController.updateControlConfig(readControlConfig());
        } else if (id == VodControlConfigIds.ENDING_RESET) {
            onEndingReset();
            sheetController.updateControlConfig(readControlConfig());
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
        ensureSheetHost();
        Timer.get().setCallback(timerSheetCallback);
        sheetController.showTimer(readTimerState());
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

    private void onTimerSheetEvent(VodTimerEvent event) {
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

    private void dismissTimerSheet() {
        Timer.get().setCallback(null);
        sheetController.dismiss();
    }

    private void onLock() {
        setLock(!isLock());
        setRequestedOrientation(getLockOrient());
        mKeyDown.setLock(isLock());
        showControl();
    }

    private void onRotate() {
        setR1Callback();
        setRotate(!isRotate());
        setRequestedOrientation(PlaybackOrientation.getRotateOrientation(this));
    }

    public void onTrack(int type) {
        if (player() == null) return;
        if (type != C.TRACK_TYPE_AUDIO && type != C.TRACK_TYPE_VIDEO && type != C.TRACK_TYPE_TEXT) return;
        String[] titles = ResUtil.getStringArray(R.array.select_track);
        int titleIndex = type - 1;
        if (titleIndex < 0 || titleIndex >= titles.length) return;
        trackSheetType = type;
        hideControl();
        ensureSheetHost();
        sheetController.showTrack(readTrackState(titles[titleIndex]));
    }

    private VodTrackState readTrackState(String title) {
        List<Track> tracks = currentTracks(trackSheetType);
        List<String> names = new ArrayList<>();
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            names.add(tracks.get(i).getName() == null ? "" : tracks.get(i).getName());
            if (tracks.get(i).isSelected()) selected.add(i);
        }
        VodTrackActions actions = VodTrackSheetKt.vodTrackActions(trackSheetType, player().isVod(), player().haveTrack(trackSheetType));
        return new VodTrackState(title, names, selected, actions.getShowChoose(), actions.getShowSubtitle(), actions.getShowOffset());
    }

    private List<Track> currentTracks(int type) {
        List<Track> items = new ArrayList<>();
        DefaultTrackNameProvider provider = new DefaultTrackNameProvider(getResources());
        List<Tracks.Group> groups = player().getCurrentTracks().getGroups();
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

    private void onTrackSheetEvent(VodTrackEvent event) {
        if (player() == null) return;
        int id = event.getId();
        if (id == VodTrackIds.CHOOSE) {
            player().pause();
            FileChooser.from(trackSubtitleLauncher).show(new String[]{MimeTypes.APPLICATION_SUBRIP, MimeTypes.TEXT_SSA, MimeTypes.TEXT_VTT, MimeTypes.APPLICATION_TTML, "audio/*", "text/*", "application/octet-stream"});
            return;
        }
        if (id == VodTrackIds.SUBTITLE) {
            onSubtitleClick();
            return;
        }
        if (id == VodTrackIds.OFFSET) {
            OffsetDialog.create().player(player()).type(trackSheetType).show(this);
            return;
        }
        if (id != VodTrackIds.TRACK) return;
        List<Track> tracks = currentTracks(trackSheetType);
        int index = event.getIndex();
        if (index < 0 || index >= tracks.size()) return;
        player().setTrack(Arrays.asList(tracks.get(index).key(player().getKey()).save()));
    }

    public void onEdition() {
        if (player() == null) return;
        hideControl();
        ensureSheetHost();
        sheetController.showEdition(readEditionState());
    }

    private VodEditionState readEditionState() {
        List<MediaEdition> items = player().getCurrentMediaEditions();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                MediaEdition item = items.get(i);
                names.add(VodEditionSheetKt.editionRowLabel(item.label == null ? "" : item.label, item.durationUs));
                if (selected < 0 && item.selected) selected = i;
            }
        }
        return new VodEditionState(getString(R.string.dialog_select_edition), names, selected);
    }

    private void onEditionSheetEvent(int index) {
        if (player() == null) return;
        List<MediaEdition> items = player().getCurrentMediaEditions();
        if (items == null || index < 0 || index >= items.size()) return;
        player().selectEdition(items.get(index));
    }

    public void onChapter() {
        if (player() == null) return;
        hideControl();
        ensureSheetHost();
        sheetController.showChapter(readChapterState());
    }

    private VodChapterState readChapterState() {
        List<MediaChapter> items = player().getCurrentMediaChapters();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                MediaChapter item = items.get(i);
                names.add(VodChapterSheetKt.chapterRowLabel(item.label == null ? "" : item.label, item.timeUs));
                if (selected < 0 && item.selected) selected = i;
            }
        }
        return new VodChapterState(getString(R.string.dialog_select_chapter), names, selected);
    }

    private void onChapterSheetEvent(int index) {
        if (player() == null) return;
        List<MediaChapter> items = player().getCurrentMediaChapters();
        if (items == null || index < 0 || index >= items.size()) return;
        player().selectChapter(items.get(index));
    }

    public void onDanmaku() {
        if (player() == null) return;
        hideControl();
        ensureSheetHost();
        sheetController.showDanmaku(readDanmakuState());
    }

    private VodDanmakuState readDanmakuState() {
        List<Danmaku> items = player().getDanmakus();
        List<String> names = new ArrayList<>();
        int selected = -1;
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                Danmaku item = items.get(i);
                names.add(item.getName() == null ? "" : item.getName());
                if (selected < 0 && item.isSelected()) selected = i;
            }
        }
        boolean showSearch = VodDanmakuSheetKt.danmakuShowSearch(player().getMetadata() != null, DanmakuSetting.getEffectiveApiUrl());
        return new VodDanmakuState(getString(R.string.danmaku_select), names, selected, showSearch);
    }

    private void onDanmakuSheetEvent(VodDanmakuEvent event) {
        if (player() == null) return;
        int id = event.getId();
        if (id == VodDanmakuIds.CHOOSE) {
            player().pause();
            FileChooser.from(danmakuFileLauncher).show("text/*");
            return;
        }
        if (id == VodDanmakuIds.SEARCH) {
            DanmakuSearchDialog.create().player(player()).show(this);
            return;
        }
        if (id == VodDanmakuIds.SETTING) {
            DanmakuSettingDialog.create().player(player()).show(this);
            return;
        }
        if (id != VodDanmakuIds.ITEM) return;
        List<Danmaku> items = player().getDanmakus();
        int index = event.getIndex();
        if (items == null || index < 0 || index >= items.size()) return;
        Danmaku item = items.get(index);
        player().setDanmaku(item.isSelected() ? Danmaku.empty() : item);
    }

    public void onChoose() {
        if (player() == null) return;
        hideControl();
        ensureSheetHost();
        sheetController.showEngine(readEngineState());
    }

    private VodEngineState readEngineState() {
        EngineSelection selection = VodEngineSheetKt.engineSelection(PlaybackAction.getEngine(player()));
        return new VodEngineState(getString(R.string.player_engine), selection.getExo(), selection.getMpv(), isDebugViewVisible());
    }

    private void onEngineSheetEvent(VodEngineEvent event) {
        if (player() == null) return;
        int id = event.getId();
        if (id == VodEngineIds.DEBUG) {
            toggleDebugView();
            return;
        }
        if (id == VodEngineIds.OTHER) {
            chooseOtherPlayer(playTitle);
            return;
        }
        int engine = id == VodEngineIds.EXO ? PlayerSetting.ENGINE_EXO : id == VodEngineIds.MPV ? PlayerSetting.ENGINE_MPV : -1;
        if (engine != PlayerSetting.ENGINE_EXO && engine != PlayerSetting.ENGINE_MPV) return;
        if (engine != PlaybackAction.getEngine(player())) hideDebugView();
        player().setEngine(engine);
    }

    private void onDanmakuShow() {
        DanmakuSetting.putShow(!DanmakuSetting.isShow());
        checkDanmakuImg();
        syncDanmakuEnabled();
    }

    public void onRepeat() {
        player().setRepeatOne(!player().isRepeatOne());
    }

    public void onSpeedChanged(float speed) {
        player().setSpeed(speed);
        if (mVod != null) mVod.setSpeed(player().getSpeed());
        pushPlaybackProgress(readPositionMs(), readDurationMs());
    }

    private void onReplay() {
        mVod.replay();
    }

    private void onRefresh() {
        mVod.refresh();
    }

    public void onDecode() {
        mProgressClock.setCallback(null);
        PlaybackAction.toggleDecode(player());
        setR1Callback();
    }

    public void onEnding() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetEnding(position, duration)) setEnding(duration - position);
        setR1Callback();
    }

    public boolean onEndingReset() {
        setR1Callback();
        setEnding(0);
        return true;
    }

    private void setEnding(long ending) {
        mVod.setEnding(ending);
    }

    public void onOpening() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetOpening(position, duration)) setOpening(position);
        setR1Callback();
    }

    public boolean onOpeningReset() {
        setR1Callback();
        setOpening(0);
        return true;
    }

    private void setOpening(long opening) {
        mVod.setOpening(opening);
    }

    public String openingLabel() {
        if (mHistory == null || mHistory.getOpening() <= 0) return getString(R.string.play_op);
        return Util.timeMs(mHistory.getOpening());
    }

    public String endingLabel() {
        if (mHistory == null || mHistory.getEnding() <= 0) return getString(R.string.play_ed);
        return Util.timeMs(mHistory.getEnding());
    }

    private void onSwipeRefresh() {
        if (mBinding.progressLayout.isEmpty()) mVod.requestDetail();
        else mVod.refresh();
    }

    private boolean shouldEnterFullscreen(Episode item) {
        boolean enter = !isFullscreen() && item.isSelected();
        if (enter) enterFullscreen();
        return enter;
    }

    private void enterFullscreen() {
        if (isFullscreen()) return;
        setFullscreen(true);
        if (isLand() && !player().isPortrait()) setTransition();
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        setRequestedOrientation(PlaybackOrientation.getEnterFullscreenOrientation(player().isPortrait()));
        setRotate(player().isPortrait());
        mPlayerScale.reset();
        App.post(mR3, 2000);
        hideControl();
    }

    private void exitFullscreen() {
        if (!isFullscreen()) return;
        setFullscreen(false);
        if (isLand() && !player().isPortrait()) setTransition();
        setRequestedOrientation(PlaybackOrientation.getExitFullscreenOrientation(isPort()));
        mBinding.video.setLayoutParams(mFrameParams);
        mPlayerScale.reset();
        App.post(mR3, 2000);
        setRotate(false);
        hideControl();
    }

    private void setTransition() {
        ChangeBounds transition = new ChangeBounds();
        transition.setDuration(150);
        ViewGroup parent = (ViewGroup) mBinding.video.getParent();
        TransitionManager.beginDelayedTransition(parent, transition);
    }

    private int getLockOrient() {
        return PlaybackOrientation.getLockOrientation(this, isLock(), isRotate(), isPort() && isAutoRotate());
    }

    void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(mR2, 0);
        hideError();
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR2);
        Traffic.reset();
    }

    void showError(String text) {
        if (mSession != null) mSession.setErrorMessage(text);
        hideProgress();
    }

    private void hideError() {
        if (mSession != null) mSession.setErrorMessage(null);
    }

    private void syncDanmakuEnabled() {
        player().setDanmakuEnabled(DanmakuSetting.isShow());
    }

    private void hideDanmaku() {
        player().setDanmakuEnabled(false);
    }

    private boolean isControlOpen() {
        return isVisible(mBinding.controlDim);
    }

    private void setComposeControlVisible(boolean open) {
        mBinding.controlDim.setVisibility(open ? View.VISIBLE : View.GONE);
        mBinding.controlTopCompose.setVisibility(View.VISIBLE);
        mBinding.controlCenterCompose.setVisibility(View.VISIBLE);
        mBinding.controlBottomCompose.setVisibility(View.VISIBLE);
        mBinding.controlLockCompose.setVisibility(View.VISIBLE);
    }

    private void padComposeControls(boolean pad) {
        ViewGroup[] views = new ViewGroup[] {mBinding.controlTopCompose, mBinding.controlCenterCompose, mBinding.controlLockCompose, mBinding.controlBottomCompose};
        for (ViewGroup view : views) {
            if (pad) setPadding(view);
            else noPadding(view);
        }
    }

    private void showControl() {
        if (service() == null || isInPictureInPictureMode()) return;
        setComposeControlVisible(true);
        applyChromeUi(true);
        setR1Callback();
    }

    private void hideControl() {
        setComposeControlVisible(false);
        applyChromeUi(false);
        App.removeCallbacks(mR1);
    }

    private void updatePlayInfo() {
        applyChromeUi(isControlOpen());
    }

    private void applyChromeUi(boolean controlsVisible) {
        boolean fullscreen = isFullscreen() && !isInPictureInPictureMode();
        if (fullscreen && !isRotate()) setPadding(mBinding.gestureCompose);
        else noPadding(mBinding.gestureCompose);
        syncComposeChrome(controlsVisible);
    }

    private void hideSheet() {
        getSupportFragmentManager().getFragments().stream().filter(fragment -> fragment instanceof BottomSheetDialogFragment).map(fragment -> (BottomSheetDialogFragment) fragment).forEach(BottomSheetDialogFragment::dismiss);
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.progress.traffic);
        App.post(mR2, 1000);
    }

    private void setOrient() {
        if (isPort() && isAutoRotate()) setRequestedOrientation(PlaybackOrientation.getPortAutoRotateOrientation());
        if (isLand() && isAutoRotate()) setRequestedOrientation(PlaybackOrientation.getLandAutoRotateOrientation());
    }

    private void setR1Callback() {
        if (isScrubbing()) return;
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    @Override
    protected void onScrubbingChanged(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(mR1);
        else if (isControlOpen()) setR1Callback();
    }

    @Override
    public void setArtwork(String url) {
        mHistory.setVodPic(url);
        setArtwork();
    }

    private void setArtwork() {
        ImgUtil.load(this, mHistory.getVodPic(), new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                mBinding.player.setDefaultArtwork(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                mBinding.player.setDefaultArtwork(errorDrawable);
            }
        });
    }

    private void saveHistory(boolean exit) {
        if (mVod == null) return;
        boolean owner = service() != null && isOwner();
        mVod.saveHistory(exit, owner, player().getPosition(), player().getDuration());
    }

    private void syncHistory() {
        if (mVod != null) mVod.syncHistory();
    }

    private void checkControl() {
        if (isControlOpen()) showControl();
    }

    void checkKeepImg() {
        if (mSession == null) return;
        mSession.refresh();
        mSession.loadKept(getHistoryKey());
    }

    private void checkDanmakuImg() {
        syncDanmakuChrome();
    }

    private void syncDanmakuChrome() {
        if (mSession == null) return;
        pushComposeControlVisibility();
    }

    private void pushComposeControlVisibility() {
        if (mSession == null) return;
        boolean showCast = false;
        boolean showDanmaku = false;
        try {
            if (service() != null) {
                showCast = !player().isEmpty();
                showDanmaku = player().haveDanmaku();
            }
        } catch (Exception ignored) {
        }
        boolean showPip = !isFullscreen() && !PiP.noPiP();
        boolean showRotate = isFullscreen();
        mSession.updateControlVisibility(showCast, showPip, showRotate, showDanmaku, DanmakuSetting.isShow());
    }

    void updateKeep() {
        if (mHistory == null) return;
        String key = getHistoryKey();
        String name = mHistory.getVodName();
        String pic = mHistory.getVodPic();
        Task.execute(() -> Keep.updateNamePic(key, name, pic));
    }

    private void updateVod(Vod item) {
        boolean id = !item.getId().isEmpty();
        boolean pic = !item.getPic().isEmpty();
        boolean name = !item.getName().isEmpty();
        if (id) getIntent().putExtra("id", item.getId());
        if (id) mHistory.replace(getHistoryKey());
        if (name) mHistory.setVodName(item.getName());
        if (name) updateDetailMeta(item.getName(), null, null);
        if (name) setPlayTitle(item.getName());
        mVod.mergeFlags(item.getFlags());
        if (pic) setArtwork(item.getPic());
        if (pic || name) setMetadata();
        if (pic || name) syncHistory();
        if (pic || name) updateKeep();
        if (id) updateNavigationKey();
        setText(item);
    }

    private final PlaybackService.NavigationCallback mNavigationCallback = new PlaybackService.NavigationCallback() {
        @Override
        public void onNext() {
            checkNext();
        }

        @Override
        public void onPrev() {
            checkPrev();
        }

        @Override
        public void onStop() {
            finish();
        }

        @Override
        public void onReplay() {
            VideoActivity.this.onReplay();
        }

        @Override
        public void onEnded() {
            mVod.playbackEnded();
        }

        @Override
        public void onAudio() {
            setAudioOnly(true);
            moveTaskToBack(true);
        }
    };

    @Override
    protected String getPlaybackKey() {
        return getHistoryKey();
    }

    @Override
    protected void onError(String msg) {
        mVod.playbackError(msg);
    }

    @Override
    protected void onReclaim() {
        mVod.reclaim(player().getPosition());
    }

    @Override
    protected void onStateChanged(int state) {
        switch (state) {
            case Player.STATE_BUFFERING:
                showProgress();
                mProgressClock.setCallback(null);
                break;
            case Player.STATE_READY:
                hideProgress();
                checkControl();
                player().reset();
                mProgressClock.setCallback(this);
                break;
            case Player.STATE_ENDED:
                hideProgress();
                setR1Callback();
                mProgressClock.setCallback(null);
                break;
        }
    }

    @Override
    protected void onPlayingChanged(boolean isPlaying) {
        if (isPlaying) mPiP.update(this, true);
        else if (isPaused()) mPiP.update(this, false);
        pushPlaybackProgress(readPositionMs(), readDurationMs());
    }

    @Override
    protected void onSizeChanged(VideoSize size) {
        changeHeight();
        checkOrientation();
    }

    @Override
    public void onSubtitleClick() {
        SubtitleDialog.create().view(mBinding.player.getSubtitleView()).player(player()).show(this);
        hideControl();
    }

    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        super.onEvents(player, events);
        if (events.containsAny(Player.EVENT_TIMELINE_CHANGED, Player.EVENT_POSITION_DISCONTINUITY, Player.EVENT_MEDIA_ITEM_TRANSITION, Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_IS_PLAYING_CHANGED)) {
            updateMiniTime();
        }
    }

    @Override
    public void onTimeChanged(long time) {
        if (!isOwner() || !player().isVod()) return;
        updateMiniTime();
        long position = readPositionMs();
        long duration = readDurationMs();
        if (duration <= 0) return;
        mVod.onTimeChanged(time, position, duration);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCastEvent(CastEvent event) {
        if (isRedirect()) return;
        ReceiveDialog.create().event(event).show(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        if (isRedirect()) return;
        if (event.getType() == RefreshEvent.Type.DETAIL) mVod.requestDetail();
        else if (event.getType() == RefreshEvent.Type.PLAYER) mVod.refresh();
        else if (event.getType() == RefreshEvent.Type.VOD) updateVod(event.getVod());
        else if (event.getType() == RefreshEvent.Type.SUBTITLE) player().setSub(Sub.from(event.getPath()));
        else if (event.getType() == RefreshEvent.Type.DANMAKU) player().setDanmaku(Danmaku.from(event.getPath()));
    }

    @Override
    protected long startPositionMs() {
        return mVod == null ? C.TIME_UNSET : mVod.startPositionMs();
    }

    private void checkOrientation() {
        if (isFullscreen() && !isRotate() && player().isPortrait()) {
            setRequestedOrientation(PlaybackOrientation.getPortraitVideoSizeOrientation());
            setRotate(true);
        } else if (isFullscreen() && isRotate() && player().isLandscape()) {
            setRequestedOrientation(PlaybackOrientation.getLandscapeVideoSizeOrientation());
            setRotate(false);
        }
    }

    private void changeHeight() {
        if (isLand() || isFullscreen() || isInPictureInPictureMode()) return;
        int videoWidth = player().getVideoWidth();
        int videoHeight = player().getVideoHeight();
        if (videoWidth == 0 || videoHeight == 0) return;
        int viewWidth = ResUtil.getScreenWidth();
        int minHeight = ResUtil.dp2px(150);
        int maxHeight = ResUtil.getScreenHeight() / 2;
        int calculated = (int) (viewWidth * ((float) videoHeight / videoWidth));
        int finalHeight = Math.clamp(calculated, minHeight, Math.max(minHeight, maxHeight));
        if (finalHeight == mBinding.video.getHeight()) return;
        if (mAnimator.isRunning()) mAnimator.cancel();
        mAnimator.setIntValues(mBinding.video.getHeight(), finalHeight);
        mAnimator.setDuration(300);
        mAnimator.start();
    }

    private MediaMetadata buildMetadata() {
        return VodPlaybackMedia.metadata(mHistory, getEpisode());
    }

    private void setMetadata() {
        player().setMetadata(buildMetadata());
    }

    private void onPaused() {
        controller().pause();
    }

    private void onPlay() {
        if (mHistory != null && isEnded()) controller().seekTo(mHistory.getOpening());
        if (!player().isEmpty() && isIdle()) controller().prepare();
        controller().play();
    }

    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    private void setFullscreen(boolean fullscreen) {
        Util.toggleFullscreen(this, this.fullscreen = fullscreen);
    }

    public boolean isUseParse() {
        return useParse;
    }

    public void setUseParse(boolean useParse) {
        this.useParse = useParse;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
        padComposeControls(fullscreen && !rotate);
        updatePlayInfo();
    }

    @Override
    public void onCasted() {
        player().stop();
    }

    public void onScale(int tag) {
        mPlayerScale.reset();
        setScale(tag);
    }

    @Override
    public void onParse(Parse item) {
        mVod.selectParse(item);
    }

    @Override
    public void onSpeedUp() {
        if (!player().isPlaying()) return;
        mSession.showSpeedHud();
        PlaybackAction.setSpeed(player(), PlayerSetting.getSpeed());
    }

    @Override
    public void onSpeedEnd() {
        mSession.clearGestureHud();
        PlaybackAction.setSpeed(player(), mHistory.getSpeed());
    }

    @Override
    public void onBright(int progress) {
        mSession.updateGestureHud(true, progress, false, 0);
    }

    @Override
    public void onVolume(int progress) {
        mSession.updateGestureHud(false, 0, true, progress);
    }

    @Override
    public void onFlingUp() {
        if (episodeCount() <= 1) onRefresh();
        else mVod.nextEpisode(true);
    }

    @Override
    public void onFlingDown() {
        if (episodeCount() <= 1) onRefresh();
        else mVod.prevEpisode(true);
    }

    private int episodeCount() {
        VodPlaybackState state = mViewModel.getPlaybackState();
        if (!state.hasFlags()) return 0;
        return state.getFlag().getEpisodes().size();
    }

    @Override
    public void onSeeking(long time) {
        mSession.showSeekHud(time > 0, player().getPositionTime(time));
        hideProgress();
    }

    @Override
    public void onSeekEnd(long time) {
        seekTo(time);
    }

    @Override
    public void onSingleTap() {
        if (isControlOpen()) hideControl();
        else showControl();
    }

    @Override
    public void onDoubleTap() {
        if (isLock()) return;
        if (!isFullscreen()) {
            enterFullscreen();
        } else if (player().isPlaying()) {
            showControl();
            onPaused();
        } else {
            hideControl();
            onPlay();
        }
    }

    @Override
    public void onTouchEnd() {
        mSession.clearGestureHud();
    }

    private void onComposeBrightness(float delta) {
        if (!gestureBrightArmed) {
            gestureBright = Util.getBrightness(this);
            gestureBrightArmed = true;
        }
        int height = mBinding.player.getHeight();
        if (height <= 0) height = mBinding.video.getHeight();
        if (height <= 0) return;
        float brightness = delta * 2f / height + gestureBright;
        if (brightness < 0f) brightness = 0f;
        if (brightness > 1f) brightness = 1f;
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = brightness;
        getWindow().setAttributes(attributes);
        onBright((int) (brightness * 100));
    }

    private void onComposeVolume(float delta) {
        if (gestureAudio == null) gestureAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (gestureAudio == null) return;
        if (!gestureVolumeArmed) {
            gestureVolume = gestureAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
            gestureVolumeMax = gestureAudio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            gestureVolumeArmed = true;
        }
        int height = mBinding.player.getHeight();
        if (height <= 0) height = mBinding.video.getHeight();
        if (height <= 0 || gestureVolumeMax <= 0) return;
        float index = gestureVolume + delta * 2f / height * gestureVolumeMax;
        if (index > gestureVolumeMax) index = gestureVolumeMax;
        if (index < 0f) index = 0f;
        gestureAudio.setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
        onVolume((int) (index / gestureVolumeMax * 100f));
    }

    private void onComposeSeekPreview(long deltaMs) {
        gestureSeeking = true;
        gestureSeekMs = deltaMs;
        onSeeking(deltaMs);
    }

    private void onComposeSeekEnd() {
        if (!gestureSeeking) return;
        long delta = gestureSeekMs;
        gestureSeeking = false;
        onSeekEnd(delta);
    }

    private void onComposeScale(float factor, float focusX, float focusY) {
        if (isLock()) return;
        mPlayerScale.apply(factor, focusX, focusY);
    }

    private void onComposeFlingUp() {
        animateGestureFling(true);
    }

    private void onComposeFlingDown() {
        animateGestureFling(false);
    }

    private void animateGestureFling(boolean up) {
        if (gestureAnimating) return;
        boolean invert = LiveSetting.isInvert();
        int dp = up ? (invert ? 24 : -24) : (invert ? -24 : 24);
        Runnable action = up ? this::onFlingUp : this::onFlingDown;
        mBinding.player.animate().translationYBy(ResUtil.dp2px(dp)).setDuration(150).withStartAction(() -> gestureAnimating = true).withEndAction(() -> mBinding.player.animate().translationY(0).setDuration(100).withStartAction(action).withEndAction(() -> gestureAnimating = false).start()).start();
    }

    private void onComposeTouchEnd() {
        gestureBrightArmed = false;
        gestureVolumeArmed = false;
        onTouchEnd();
    }

    public void onShare(CharSequence title) {
        PlayerHelper.share(this, player().getUrl(), player().getHeaders(), title);
        setRedirect(true);
    }

    private void onDownload() {
        if (episodeCount() == 0) return;
        syncCatalog();
        ensureSheetHost();
        sheetController.showEpisodes(true);
    }

    private void ensureSheetHost() {
        mBinding.sheetCompose.bringToFront();
        mBinding.sheetCompose.setVisibility(View.VISIBLE);
    }

    private void onSheetPlayEpisode(int index) {
        onComposeEpisode(index);
    }

    private void onSheetDownloadEpisode(int index) {
        VodPlaybackState state = mViewModel.getPlaybackState();
        if (!state.hasFlags()) return;
        List<Episode> episodes = state.getFlag().getEpisodes();
        if (index < 0 || index >= episodes.size()) return;
        onDownloadEpisode(episodes.get(index));
    }

    private void onSheetDownloadList() {
        DownloadDialog.openList(this);
    }

    private void onSheetDownloadConfirm(VodDownloadConfirm confirm) {
        if (DownloadDialog.send(this, confirm.getFileName(), confirm.getUrl(), confirm.getHeaders())) {
            if (mSession != null) mSession.clearDownloadReady();
            sheetController.dismiss();
        }
    }

    private void onSheetInfoShare(String title) {
        onShare(title);
    }

    private void onSheetControlMore(int id) {
        if (id == VodControlMoreIds.EPISODES) {
            syncCatalog();
            ensureSheetHost();
            sheetController.showEpisodes(false);
        } else if (id == VodControlMoreIds.SPEED) {
            onCycleSpeed();
        } else if (id == VodControlMoreIds.SCALE) {
            onScale((getScale() + 1) % 5);
        } else if (id == VodControlMoreIds.DECODE) {
            onDecode();
        } else if (id == VodControlMoreIds.REPEAT) {
            onRepeat();
        } else if (id == VodControlMoreIds.OPENING) {
            onOpening();
        } else if (id == VodControlMoreIds.ENDING) {
            onEnding();
        } else if (id == VodControlMoreIds.INFO) {
            onPlaybackInfo();
        } else if (id == VodControlMoreIds.TEXT) onTrack(C.TRACK_TYPE_TEXT);
        else if (id == VodControlMoreIds.SUBTITLE) onSubtitleClick();
        else if (id == VodControlMoreIds.AUDIO) onTrack(C.TRACK_TYPE_AUDIO);
        else if (id == VodControlMoreIds.VIDEO) onTrack(C.TRACK_TYPE_VIDEO);
        else if (id == VodControlMoreIds.DANMAKU) onDanmaku();
        else if (id == VodControlMoreIds.REPLAY) onReplay();
        else if (id == VodControlMoreIds.RESET) onRefresh();
        else if (id == VodControlMoreIds.ALL_SETTINGS) onAllSettings();
    }

    private void onCycleSpeed() {
        if (player() == null) return;
        player().addSpeed();
        if (mVod != null) mVod.setSpeed(player().getSpeed());
        pushPlaybackProgress(readPositionMs(), readDurationMs());
        setR1Callback();
    }

    private void onPlaybackInfo() {
        CharSequence title = getNameText();
        ensureSheetHost();
        sheetController.showInfo(title, player().getUrl(), player().getHeaders());
    }

    private void onDownloadEpisode(Episode item) {
        int seq = ++downloadSeq;
        stopDownloadParse();
        VodPlaybackState state = mViewModel.getPlaybackState();
        Episode activated = state.hasEpisode() ? state.getEpisode() : null;
        if (item.equals(activated)) {
            DownloadResolveOutcome.Ready ready = mSession.prepareDownloadFromPlayer(
                    player().getUrl(),
                    player().getHeaders(),
                    VodDownloadResolver.buildFileName(getNameText(), item.getName())
            );
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
        String key = getKey();
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
        DownloadResolveOutcome outcome = mSession.resolveDownloadResult(result);
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
                    DownloadResolveOutcome.Ready ready = mSession.prepareDownloadFromPlayer(
                            url,
                            headers,
                            VodDownloadResolver.buildFileName(getNameText(), item.getName())
                    );
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
        return seq == downloadSeq && !isFinishing() && !isDestroyed();
    }

    private void stopDownloadParse() {
        if (downloadParse == null) return;
        downloadParse.stop();
        downloadParse = null;
    }

    private void showDownload(Episode item, String url, Map<String, String> headers) {
        if (TextUtils.isEmpty(url)) return;
        String save = VodDownloadResolver.buildFileName(getNameText(), item == null ? "" : item.getName());
        VodPlaybackState state = mViewModel.getPlaybackState();
        Episode activated = state.hasEpisode() ? state.getEpisode() : null;
        long duration = item != null && item.equals(activated) ? player().getDuration() : 0;
        mSession.presentDownloadConfirm(save, url, duration, headers);
        ensureSheetHost();
        sheetController.showDownloadConfirm(save, url, duration, headers);
    }

    private void onPip() {
        mPiP.enter(this, player().getVideoWidth(), player().getVideoHeight(), getScale(), true);
    }

    private long readPositionMs() {
        try {
            if (controller() != null) {
                long position = controller().getCurrentPosition();
                if (position >= 0) return position;
            }
            if (service() != null) {
                long position = player().getPosition();
                if (position >= 0) return position;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private long readDurationMs() {
        try {
            if (controller() != null) {
                long duration = controller().getDuration();
                if (duration > 0 && duration != C.TIME_UNSET) return duration;
            }
            if (service() != null) {
                long duration = player().getDuration();
                if (duration > 0 && duration != C.TIME_UNSET) return duration;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private void updateMiniTime() {
        updateMiniTime(readPositionMs(), readDurationMs());
    }

    private void syncComposeChrome(boolean visible) {
        if (mSession == null) return;
        boolean fullscreen = isFullscreen() && !isInPictureInPictureMode();
        mSession.updatePlaybackChrome(visible, isLock(), readPlaying(), readPositionMs(), readDurationMs(), readSpeed(), fullscreen);
        pushComposeControlVisibility();
    }

    private void pushPlaybackProgress(long position, long duration) {
        if (mSession == null) return;
        long safePosition = position < 0 ? 0 : position;
        long safeDuration = duration < 0 || duration == C.TIME_UNSET ? 0 : duration;
        mSession.updatePlaybackProgress(safePosition, safeDuration, readPlaying(), readSpeed());
    }

    private boolean readPlaying() {
        try {
            return service() != null && player().isPlaying();
        } catch (Exception ignored) {
            return false;
        }
    }

    private float readSpeed() {
        try {
            return service() != null ? player().getSpeed() : 1f;
        } catch (Exception ignored) {
            return 1f;
        }
    }

    private void onComposeSeek(long positionMs) {
        long target = Math.max(0, positionMs);
        if (controller() != null) {
            controller().seekTo(target);
            controller().play();
        }
        updateMiniTime();
        setR1Callback();
    }

    private void onComposeScrub(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(mR1);
        else if (isControlOpen()) setR1Callback();
    }

    private void updateMiniTime(long position, long duration) {
        if (position < 0) position = 0;
        if (duration < 0 || duration == C.TIME_UNSET) duration = 0;
        pushPlaybackProgress(position, duration);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) PlayerHelper.onExternalResult(data, service()::dispatchNext, controller()::seekTo);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (isRedirect()) return;
        if (isLock()) App.post(this::onLock, 500);
        if (service() != null && player().haveTrack(C.TRACK_TYPE_VIDEO)) mPiP.enter(this, player().getVideoWidth(), player().getVideoHeight(), getScale());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (!isFullscreen()) setVideoView(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            hideControl();
            hideDanmaku();
            hideSheet();
        } else {
            syncDanmakuEnabled();
            updatePlayInfo();
            if (isStop()) finish();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isAutoRotate() && isPort() && newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && !isRotate() && !isLock()) exitFullscreen();
        if (isAutoRotate() && isPort() && newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) enterFullscreen();
        if (isFullscreen()) Util.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFullscreen() && hasFocus) Util.hideSystemUI(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProgressClock.stop().start();
        setAudioOnly(false);
        setStop(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (PlayerSetting.isBackgroundOff()) mProgressClock.stop();
        if (!isAudioOnly()) setStop(true);
    }

    @Override
    protected void onBackInvoked() {
        if (isControlOpen()) {
            hideControl();
        } else if (isFullscreen() && !isLock()) {
            exitFullscreen();
        } else if (!isLock()) {
            mViewModel.stopSearch();
            if (isTaskRoot()) startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        stopDownloadParse();
        mProgressClock.release();
        saveHistory(true);
        Timer.get().setCallback(null);
        Timer.get().reset();
        DanmakuApi.cancel();
        RefreshEvent.keep();
        App.removeCallbacks(mR1, mR2, mR3, mR4);
        super.onDestroy();
    }
}
