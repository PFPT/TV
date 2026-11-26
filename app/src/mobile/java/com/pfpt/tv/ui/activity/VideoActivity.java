package com.pfpt.tv.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.media3.session.MediaController;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.DefaultTrackNameProvider;
import androidx.media3.ui.PlayerSeekView;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.pfpt.tv.App;
import com.pfpt.tv.Constant;
import com.pfpt.tv.R;
import com.pfpt.tv.api.DanmakuApi;
import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.playback.vod.VodLaunch;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.CastVideo;
import com.pfpt.tv.bean.Config;
import com.pfpt.tv.bean.Danmaku;
import com.pfpt.tv.bean.Device;
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
import com.pfpt.tv.dlna.DLNACast;
import com.pfpt.tv.dlna.DLNACastManager;
import com.pfpt.tv.event.CastEvent;
import com.pfpt.tv.event.RefreshEvent;
import com.pfpt.tv.impl.CustomTarget;
import com.pfpt.tv.impl.ParseCallback;
import com.pfpt.tv.model.VideoViewModel;
import com.pfpt.tv.player.parse.ParseJob;
import com.pfpt.tv.player.util.PlayerHelper;
import com.pfpt.tv.service.PlaybackService;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.setting.PlayerSetting;
import com.pfpt.tv.server.Server;
import com.pfpt.tv.ui.activity.ScanActivity;
import com.pfpt.tv.ui.custom.CustomKeyDown;
import com.pfpt.tv.ui.dialog.CastDialog;
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
import com.pfpt.tv.impl.Callback;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.playback.vod.session.DownloadResolveOutcome;
import com.pfpt.tv.playback.vod.session.VodDownloadResolver;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.ui.vod.VodCastSession;
import com.pfpt.tv.ui.vod.VodDanmakuPrefs;
import com.pfpt.tv.ui.vod.VodReceiveSession;
import com.pfpt.tv.ui.vod.compose.VodComposeHostKt;
import com.pfpt.tv.ui.vod.compose.VodControlConfig;
import com.pfpt.tv.ui.vod.compose.VodControlConfigEvent;
import com.pfpt.tv.ui.vod.compose.VodControlConfigIds;
import com.pfpt.tv.ui.vod.compose.VodControlConfigSheetKt;
import com.pfpt.tv.ui.vod.compose.VodControlMoreIds;
import com.pfpt.tv.ui.vod.compose.VodControlMoreVisibility;
import com.pfpt.tv.ui.vod.compose.VodDownloadConfirm;
import com.pfpt.tv.ui.vod.compose.VodPlayerScale;
import com.pfpt.tv.ui.vod.compose.VodOffsetEvent;
import com.pfpt.tv.ui.vod.compose.VodOffsetIds;
import com.pfpt.tv.ui.vod.compose.VodOffsetSheetKt;
import com.pfpt.tv.ui.vod.compose.VodOffsetState;
import com.pfpt.tv.ui.vod.compose.VodReceiveState;
import com.pfpt.tv.ui.vod.compose.VodSheetController;
import com.pfpt.tv.ui.vod.compose.VodSheetKind;
import com.pfpt.tv.ui.vod.compose.VodSheetsKt;
import com.pfpt.tv.ui.vod.compose.EngineSelection;
import com.pfpt.tv.ui.vod.compose.VodCastEvent;
import com.pfpt.tv.ui.vod.compose.VodCastIds;
import com.pfpt.tv.ui.vod.compose.VodCastSheetKt;
import com.pfpt.tv.ui.vod.compose.VodCastState;
import com.pfpt.tv.ui.vod.compose.VodChapterSheetKt;
import com.pfpt.tv.ui.vod.compose.VodChapterState;
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
import com.pfpt.tv.ui.vod.compose.VodEditionSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEditionState;
import com.pfpt.tv.ui.vod.compose.VodEngineEvent;
import com.pfpt.tv.ui.vod.compose.VodEngineIds;
import com.pfpt.tv.ui.vod.compose.VodEngineSheetKt;
import com.pfpt.tv.ui.vod.compose.VodEngineState;
import com.pfpt.tv.ui.vod.compose.VodSubtitleIds;
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
import com.pfpt.tv.utils.ScanTask;
import com.pfpt.tv.utils.Sniffer;
import com.pfpt.tv.utils.Task;
import com.pfpt.tv.utils.Timer;
import com.pfpt.tv.utils.Traffic;
import com.pfpt.tv.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class VideoActivity extends PlaybackActivity implements VodPlaybackActivityAccess, Clock.Callback, CustomKeyDown.Listener, TrackDialog.Listener, ParseDialog.Listener, CastDialog.Listener {

    ActivityVideoBinding mBinding;
    private ViewGroup.LayoutParams mFrameParams;
    private Observer<Result> mObserveDetail;
    private Observer<Result> mObservePlayer;
    private Observer<Result> mObserveSearch;
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
    private final VodCastSession castSession = new VodCastSession(this, sheetController, this::onCasted, this::launchCastScan);
    private final VodReceiveSession receiveSession = new VodReceiveSession(this, new VodReceiveSession.Host() {
        @Override
        public void show(VodReceiveState state) {
            sheetController.showReceive(state);
        }

        @Override
        public void update(VodReceiveState state) {
            sheetController.updateReceive(state);
        }

        @Override
        public boolean isOpen() {
            return sheetController.getKind() == VodSheetKind.Receive;
        }

        @Override
        public void dismiss() {
            if (sheetController.getKind() == VodSheetKind.Receive) sheetController.dismiss();
        }
    });
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
    private final ActivityResultLauncher<Intent> castScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
        castSession.scan(result.getData().getStringExtra("address"));
    });
    private final MobileVodSheets sheets = new MobileVodSheets(new SheetHost());
    private final MobileVodChrome chrome = new MobileVodChrome(new ChromeHost());
    private final MobileVodGesture gestures = new MobileVodGesture();
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    Runnable mR4;
    Clock mProgressClock;
    private PiP mPiP;

    public static void push(FragmentActivity activity, String text) {
        VodLaunch.push(activity, VideoActivity.class, text);
    }

    public static void file(FragmentActivity activity, String path) {
        VodLaunch.file(activity, VideoActivity.class, path);
    }

    public static void cast(Activity activity, History history) {
        VodLaunch.start(activity, VideoActivity.class, history.getSiteKey(), history.getVodId(), history.getVodName(), history.getVodPic());
    }

    public static void collect(Activity activity, String key, String id, String name, String pic) {
        VodLaunch.collect(activity, VideoActivity.class, key, id, name, pic);
    }

    public static void start(Activity activity, String url) {
        VodLaunch.start(activity, VideoActivity.class, url);
    }

    public static void start(Activity activity, String key, String id, String name) {
        VodLaunch.start(activity, VideoActivity.class, key, id, name);
    }

    public static void start(Activity activity, String key, String id, String name, String pic) {
        VodLaunch.start(activity, VideoActivity.class, key, id, name, pic);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark) {
        VodLaunch.start(activity, VideoActivity.class, key, id, name, pic, mark);
    }

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean collect) {
        VodLaunch.start(activity, VideoActivity.class, key, id, name, pic, mark, collect, false);
    }

    @Override
    public String getName() {
        return VodLaunch.text(getIntent(), "name");
    }

    @Override
    public String getPic() {
        return VodLaunch.text(getIntent(), "pic");
    }

    @Override
    public String getMark() {
        return VodLaunch.text(getIntent(), "mark");
    }

    @Override
    public String getKey() {
        return VodLaunch.text(getIntent(), "key");
    }

    @Override
    public String getId() {
        return VodLaunch.text(getIntent(), "id");
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
        VodSheetsKt.setVodSheetHost(mBinding.sheetCompose, mSession, sheetController, this::onSheetPlayEpisode, this::onSheetDownloadEpisode, this::onSheetDownloadList, this::onSheetDownloadConfirm, this::onSheetInfoShare, this::onSheetControlMore, this::onSheetControlConfig, this::onTimerSheetEvent, this::dismissTimerSheet, this::onTrackSheetEvent, this::onEditionSheetEvent, this::onChapterSheetEvent, this::onEngineSheetEvent, this::onDanmakuSheetEvent, this::onSubtitleSheetEvent, this::onOffsetSheetEvent, castSession::onEvent, castSession::dismiss, receiveSession::play, receiveSession::dismiss, this::onDanmakuSearchEvent, this::dismissDanmakuSearch, this::onDanmakuSettingEvent);
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

    private void ensureSheetHost() {
        mBinding.sheetCompose.bringToFront();
        mBinding.sheetCompose.setVisibility(View.VISIBLE);
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
        if (player() == null || mHistory == null) return;
        hideControl();
        ensureSheetHost();
        castSession.open(getNameText(), player().getUrl(), player().getPosition(), player().getHeaders(), mHistory);
    }

    private void launchCastScan() {
        castScanLauncher.launch(new Intent(this, ScanActivity.class));
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
        sheets.onSetting();
    }

    private void onSheetControlConfig(VodControlConfigEvent event) {
        sheets.onSheetControlConfig(event);
    }

    private void onTimerSheetEvent(VodTimerEvent event) {
        sheets.onTimerSheetEvent(event);
    }

    private void dismissTimerSheet() {
        sheets.dismissTimerSheet();
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
        sheets.onTrack(type);
    }

    private void onTrackSheetEvent(VodTrackEvent event) {
        sheets.onTrackSheetEvent(event);
    }

    public void onEdition() {
        sheets.onEdition();
    }

    private void onEditionSheetEvent(int index) {
        sheets.onEditionSheetEvent(index);
    }

    public void onChapter() {
        sheets.onChapter();
    }

    private void onChapterSheetEvent(int index) {
        sheets.onChapterSheetEvent(index);
    }

    public void onDanmaku() {
        sheets.onDanmaku();
    }

    private void onDanmakuSheetEvent(VodDanmakuEvent event) {
        sheets.onDanmakuSheetEvent(event);
    }

    private void onDanmakuSearchEvent(VodDanmakuSearchEvent event) {
        sheets.onDanmakuSearchEvent(event);
    }

    private void dismissDanmakuSearch() {
        sheets.dismissDanmakuSearch();
    }

    private void onDanmakuSettingEvent(VodDanmakuSettingEvent event) {
        sheets.onDanmakuSettingEvent(event);
    }

    public void onChoose() {
        sheets.onChoose();
    }

    private void onEngineSheetEvent(VodEngineEvent event) {
        sheets.onEngineSheetEvent(event);
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
        chrome.enterFullscreen();
    }

    private void exitFullscreen() {
        chrome.exitFullscreen();
    }

    private int getLockOrient() {
        return chrome.getLockOrient();
    }

    void showProgress() {
        chrome.showProgress();
    }

    private void hideProgress() {
        chrome.hideProgress();
    }

    void showError(String text) {
        chrome.showError(text);
    }

    private void syncDanmakuEnabled() {
        chrome.syncDanmakuEnabled();
    }

    private void hideDanmaku() {
        chrome.hideDanmaku();
    }

    private boolean isControlOpen() {
        return chrome.isControlOpen();
    }

    private void padComposeControls(boolean pad) {
        chrome.padComposeControls(pad);
    }

    private void showControl() {
        chrome.showControl();
    }

    private void hideControl() {
        chrome.hideControl();
    }

    private void updatePlayInfo() {
        chrome.updatePlayInfo();
    }

    private void hideSheet() {
        chrome.hideSheet();
    }

    private void setTraffic() {
        chrome.setTraffic();
    }

    private void setOrient() {
        chrome.setOrient();
    }

    private void setR1Callback() {
        chrome.setR1Callback();
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
        chrome.pushComposeControlVisibility();
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
        sheets.onSubtitleClick();
    }

    private void onSubtitleSheetEvent(int id) {
        sheets.onSubtitleSheetEvent(id);
    }

    private void onOffsetSheetEvent(VodOffsetEvent event) {
        sheets.onOffsetSheetEvent(event);
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
        if (event == null || event.history() == null) return;
        ensureSheetHost();
        receiveSession.show(event);
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
        int percent = gestures.brightness(this, gestureHeight(), delta);
        if (percent >= 0) onBright(percent);
    }

    private void onComposeVolume(float delta) {
        int percent = gestures.volume(this, gestureHeight(), delta);
        if (percent >= 0) onVolume(percent);
    }

    private void onComposeSeekPreview(long deltaMs) {
        onSeeking(gestures.seekPreview(deltaMs));
    }

    private void onComposeSeekEnd() {
        long delta = gestures.seekEnd();
        if (delta != Long.MIN_VALUE) onSeekEnd(delta);
    }

    private void onComposeScale(float factor, float focusX, float focusY) {
        gestures.scale(mPlayerScale, isLock(), factor, focusX, focusY);
    }

    private void onComposeFlingUp() {
        gestures.fling(mBinding.player, true, this::onFlingUp);
    }

    private void onComposeFlingDown() {
        gestures.fling(mBinding.player, false, this::onFlingDown);
    }

    private void onComposeTouchEnd() {
        gestures.reset();
        onTouchEnd();
    }

    private int gestureHeight() {
        int height = mBinding.player.getHeight();
        if (height <= 0) height = mBinding.video.getHeight();
        return height;
    }

    public void onShare(CharSequence title) {
        PlayerHelper.share(this, player().getUrl(), player().getHeaders(), title);
        setRedirect(true);
    }

    private void onDownload() {
        sheets.onDownload();
    }

    private void onSheetPlayEpisode(int index) {
        sheets.onSheetPlayEpisode(index);
    }

    private void onSheetDownloadEpisode(int index) {
        sheets.onSheetDownloadEpisode(index);
    }

    private void onSheetDownloadList() {
        sheets.onSheetDownloadList();
    }

    private void onSheetDownloadConfirm(VodDownloadConfirm confirm) {
        sheets.onSheetDownloadConfirm(confirm);
    }

    private void onSheetInfoShare(String title) {
        sheets.onSheetInfoShare(title);
    }

    private void onSheetControlMore(int id) {
        sheets.onSheetControlMore(id);
    }

    private void stopDownloadParse() {
        sheets.stopDownloadParse();
    }

    private void onPip() {
        chrome.onPip();
    }

    private long readPositionMs() {
        return chrome.readPositionMs();
    }

    private long readDurationMs() {
        return chrome.readDurationMs();
    }

    private void updateMiniTime() {
        chrome.updateMiniTime();
    }

    private void syncComposeChrome(boolean visible) {
        chrome.syncComposeChrome(visible);
    }

    private void pushPlaybackProgress(long position, long duration) {
        chrome.pushPlaybackProgress(position, duration);
    }

    private void onComposeSeek(long positionMs) {
        chrome.onComposeSeek(positionMs);
    }

    private void onComposeScrub(boolean scrubbing) {
        chrome.onComposeScrub(scrubbing);
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
        castSession.release();
        DanmakuApi.cancel();
        RefreshEvent.keep();
        App.removeCallbacks(mR1, mR2, mR3, mR4);
        super.onDestroy();
    }

    private final class SheetHost implements MobileVodSheets.Host {
        @Override
        public PlayerManager player() {
            return VideoActivity.this.player();
        }

        @Override
        public int scale() {
            return getScale();
        }

        @Override
        public String text(int resId) {
            return getString(resId);
        }

        @Override
        public Resources resources() {
            return getResources();
        }

        @Override
        public VodSheetController sheet() {
            return sheetController;
        }

        @Override
        public boolean useParse() {
            return isUseParse();
        }

        @Override
        public String openingLabel() {
            return VideoActivity.this.openingLabel();
        }

        @Override
        public String endingLabel() {
            return VideoActivity.this.endingLabel();
        }

        @Override
        public void onSpeedChanged(float speed) {
            VideoActivity.this.onSpeedChanged(speed);
        }

        @Override
        public void onParse(Parse item) {
            VideoActivity.this.onParse(item);
        }

        @Override
        public void onScale(int index) {
            VideoActivity.this.onScale(index);
        }

        @Override
        public void onDecode() {
            VideoActivity.this.onDecode();
        }

        @Override
        public void onRepeat() {
            VideoActivity.this.onRepeat();
        }

        @Override
        public void onOpening() {
            VideoActivity.this.onOpening();
        }

        @Override
        public boolean onOpeningReset() {
            return VideoActivity.this.onOpeningReset();
        }

        @Override
        public void onEnding() {
            VideoActivity.this.onEnding();
        }

        @Override
        public boolean onEndingReset() {
            return VideoActivity.this.onEndingReset();
        }

        @Override
        public void hideControl() {
            VideoActivity.this.hideControl();
        }

        @Override
        public ActivityResultLauncher<Intent> trackSubtitleLauncher() {
            return trackSubtitleLauncher;
        }

        @Override
        public ActivityResultLauncher<Intent> danmakuFileLauncher() {
            return danmakuFileLauncher;
        }

        @Override
        public boolean debugVisible() {
            return isDebugViewVisible();
        }

        @Override
        public void toggleDebug() {
            toggleDebugView();
        }

        @Override
        public void hideDebug() {
            hideDebugView();
        }

        @Override
        public void chooseOther(String title) {
            chooseOtherPlayer(title);
        }

        @Override
        public String playTitle() {
            return playTitle;
        }

        @Override
        public void ensureSheet() {
            ensureSheetHost();
        }

        @Override
        public void playEpisode(int index) {
            onComposeEpisode(index);
        }

        @Override
        public VideoViewModel model() {
            return mViewModel;
        }

        @Override
        public VodSessionViewModel session() {
            return mSession;
        }

        @Override
        public String nameText() {
            return getNameText();
        }

        @Override
        public String siteKey() {
            return getKey();
        }

        @Override
        public int episodeCount() {
            return VideoActivity.this.episodeCount();
        }

        @Override
        public void syncCatalog() {
            VideoActivity.this.syncCatalog();
        }

        @Override
        public boolean alive() {
            return !isFinishing() && !isDestroyed();
        }

        @Override
        public void share(CharSequence title) {
            onShare(title);
        }

        @Override
        public void replay() {
            onReplay();
        }

        @Override
        public void refresh() {
            onRefresh();
        }

        @Override
        public void rememberSpeed() {
            if (mVod != null) mVod.setSpeed(player().getSpeed());
        }

        @Override
        public void pushProgress() {
            pushPlaybackProgress(readPositionMs(), readDurationMs());
        }

        @Override
        public void armHide() {
            setR1Callback();
        }

        @Override
        public SubtitleView subtitleView() {
            return mBinding.player.getSubtitleView();
        }

        @Override
        public Activity activity() {
            return VideoActivity.this;
        }
    }


    private final class ChromeHost implements MobileVodChrome.Host {
        @Override
        public ActivityVideoBinding binding() {
            return mBinding;
        }

        @Override
        public PlayerManager player() {
            return VideoActivity.this.player();
        }

        @Override
        public boolean serviceReady() {
            return service() != null;
        }

        @Override
        public VodSessionViewModel session() {
            return mSession;
        }

        @Override
        public PiP pip() {
            return mPiP;
        }

        @Override
        public VodPlayerScale playerScale() {
            return mPlayerScale;
        }

        @Override
        public ViewGroup.LayoutParams frame() {
            return mFrameParams;
        }

        @Override
        public Runnable hideTask() {
            return mR1;
        }

        @Override
        public Runnable trafficTask() {
            return mR2;
        }

        @Override
        public Runnable orientTask() {
            return mR3;
        }

        @Override
        public boolean fullscreen() {
            return isFullscreen();
        }

        @Override
        public void setFullscreen(boolean value) {
            VideoActivity.this.setFullscreen(value);
        }

        @Override
        public boolean rotate() {
            return isRotate();
        }

        @Override
        public void setRotate(boolean value) {
            VideoActivity.this.setRotate(value);
        }

        @Override
        public boolean locked() {
            return isLock();
        }

        @Override
        public int scale() {
            return getScale();
        }

        @Override
        public boolean port() {
            return isPort();
        }

        @Override
        public boolean land() {
            return isLand();
        }

        @Override
        public boolean autoRotate() {
            return isAutoRotate();
        }

        @Override
        public boolean scrubbing() {
            return isScrubbing();
        }

        @Override
        public boolean inPip() {
            return isInPictureInPictureMode();
        }

        @Override
        public boolean visible(View view) {
            return isVisible(view);
        }

        @Override
        public MediaController controller() {
            return VideoActivity.this.controller();
        }

        @Override
        public void orientation(int value) {
            setRequestedOrientation(value);
        }

        @Override
        public void pad(ViewGroup view) {
            setPadding(view);
        }

        @Override
        public void clearPad(ViewGroup view) {
            noPadding(view);
        }

        @Override
        public ViewGroup[] controlViews() {
            return new ViewGroup[] {mBinding.controlTopCompose, mBinding.controlCenterCompose, mBinding.controlLockCompose, mBinding.controlBottomCompose};
        }

        @Override
        public ViewGroup gesture() {
            return mBinding.gestureCompose;
        }

        @Override
        public FragmentActivity activity() {
            return VideoActivity.this;
        }
    }

}
