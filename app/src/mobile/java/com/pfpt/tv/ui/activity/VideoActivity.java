package com.pfpt.tv.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.PlayerSeekView;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
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
import com.pfpt.tv.ui.adapter.EpisodeAdapter;
import com.pfpt.tv.ui.adapter.FlagAdapter;
import com.pfpt.tv.ui.adapter.QualityAdapter;
import com.pfpt.tv.ui.adapter.QuickAdapter;
import com.pfpt.tv.ui.base.ViewType;
import com.pfpt.tv.ui.custom.CustomKeyDown;
import com.pfpt.tv.ui.custom.CustomMovement;
import com.pfpt.tv.ui.custom.SpaceItemDecoration;
import com.pfpt.tv.ui.dialog.CastDialog;
import com.pfpt.tv.ui.dialog.ChapterDialog;
import com.pfpt.tv.ui.dialog.ControlDialog;
import com.pfpt.tv.ui.dialog.DanmakuDialog;
import com.pfpt.tv.ui.dialog.DownloadDialog;
import com.pfpt.tv.ui.dialog.EditionDialog;
import com.pfpt.tv.ui.dialog.ParseDialog;
import com.pfpt.tv.ui.dialog.PlayerEngineDialog;
import com.pfpt.tv.ui.dialog.ReceiveDialog;
import com.pfpt.tv.ui.dialog.SubtitleDialog;
import com.pfpt.tv.ui.dialog.TrackDialog;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.PlaybackOrientation;
import com.pfpt.tv.playback.vod.VodPlaybackActivityAccess;
import com.pfpt.tv.playback.vod.VodPlaybackController;
import com.pfpt.tv.playback.vod.VodPlaybackMedia;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.playback.vod.session.DownloadResolveOutcome;
import com.pfpt.tv.playback.vod.session.VodDownloadResolver;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.ui.vod.compose.VodComposeHostKt;
import com.pfpt.tv.ui.vod.compose.VodDownloadConfirm;
import com.pfpt.tv.ui.vod.compose.VodSheetController;
import com.pfpt.tv.ui.vod.compose.VodSheetsKt;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VideoActivity extends PlaybackActivity implements VodPlaybackActivityAccess, Clock.Callback, CustomKeyDown.Listener, TrackDialog.Listener, ControlDialog.Listener, ParseDialog.Listener, FlagAdapter.OnClickListener, EpisodeAdapter.OnClickListener, QualityAdapter.OnClickListener, QuickAdapter.OnClickListener, CastDialog.Listener {

    ActivityVideoBinding mBinding;
    private ViewGroup.LayoutParams mFrameParams;
    private Observer<Result> mObserveDetail;
    private Observer<Result> mObservePlayer;
    private Observer<Result> mObserveSearch;
    EpisodeAdapter mEpisodeAdapter;
    private ParseJob downloadParse;
    private int downloadSeq;
    QualityAdapter mQualityAdapter;
    QuickAdapter mQuickAdapter;
    FlagAdapter mFlagAdapter;
    private VodPlaybackController mVod;
    VideoViewModel mViewModel;
    private VodSessionViewModel mSession;
    private String siteRawForCompose;
    private String remarkRawForCompose;
    private ValueAnimator mAnimator;
    private CustomKeyDown mKeyDown;
    History mHistory;
    private boolean fullscreen;
    private boolean useParse;
    private boolean rotate;
    private String playTitle = "";
    private final VodSheetController sheetController = new VodSheetController();
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
    Clock mClock;
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
        return mBinding.name.getText().toString();
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
        mQuickAdapter.clear();
    }

    @Override
    public void addQuickItems(List<Vod> items) {
        mQuickAdapter.addAll(items);
    }

    @Override
    public boolean isQuickAdapterEmpty() {
        return mQuickAdapter.isEmpty();
    }

    @Override
    public void setQuickVisible(int visibility) {
        mBinding.quick.setVisibility(visibility);
    }

    @Override
    public void clearClockCallback() {
        mClock.setCallback(null);
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
        return mEpisodeAdapter.isEmpty() ? new Episode() : mEpisodeAdapter.getActivated();
    }

    @Override
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
        mFrameParams = mBinding.video.getLayoutParams();
        mBinding.progressLayout.showProgress();
        mBinding.swipeLayout.setEnabled(false);
        mObserveDetail = this::onDetailObserved;
        mObservePlayer = this::onPlayerObserved;
        mObserveSearch = this::onSearchObserved;
        mClock = Clock.create().format("HH:mm");
        mBinding.widget.clock.setVisibility(View.GONE);
        mBinding.widget.playInfo.setVisibility(View.GONE);
        mBinding.widget.error.setVisibility(View.GONE);
        mR1 = this::hideControl;
        mR2 = this::setTraffic;
        mR3 = this::setOrient;
        mR4 = this::showEmpty;
        mPiP = new PiP();
        checkDanmakuImg();
        setRecyclerView();
        setVideoView();
        setViewModel();
        showProgress();
        setAnimator();
    }

    @Override
    protected void initEvent() {
        mBinding.name.setOnClickListener(view -> onName());
        mBinding.download.setOnClickListener(view -> onDownload());
        mBinding.more.setOnClickListener(view -> onMore());
        mBinding.actor.setOnClickListener(view -> onActor());
        mBinding.content.setOnClickListener(view -> onContent());
        mBinding.reverse.setOnClickListener(view -> onReverse());
        mBinding.director.setOnClickListener(view -> onDirector());
        mBinding.name.setOnLongClickListener(view -> onChange());
        mBinding.content.setOnLongClickListener(view -> onCopy());
        mBinding.swipeLayout.setOnRefreshListener(this::onSwipeRefresh);
    }

    private WindowInsetsCompat setStatusBar(WindowInsetsCompat insets) {
        int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        ViewGroup.LayoutParams lp = mBinding.statusBar.getLayoutParams();
        lp.height = top;
        mBinding.statusBar.setLayoutParams(lp);
        return insets;
    }

    private void setRecyclerView() {
        mBinding.flag.setHasFixedSize(true);
        mBinding.flag.setItemAnimator(null);
        mBinding.flag.addItemDecoration(new SpaceItemDecoration(8));
        mBinding.flag.setAdapter(mFlagAdapter = new FlagAdapter(this));
        mBinding.quick.setAdapter(mQuickAdapter = new QuickAdapter(this));
        mBinding.episode.setHasFixedSize(true);
        mBinding.episode.setItemAnimator(null);
        mBinding.episode.addItemDecoration(new SpaceItemDecoration(8));
        mBinding.episode.setAdapter(mEpisodeAdapter = new EpisodeAdapter(this, ViewType.HORI));
        mBinding.quality.setHasFixedSize(true);
        mBinding.quality.setItemAnimator(null);
        mBinding.quality.addItemDecoration(new SpaceItemDecoration(8));
        mBinding.quality.setAdapter(mQualityAdapter = new QualityAdapter(this));
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
        VodComposeHostKt.setVodScreen(mBinding.detailCompose, mSession, this::onKeep, this::onName, () -> onChange(), this::onComposeSiteClick, this::onComposeRemarkClick, this::onComposeFlag, this::onComposeEpisode, this::onComposeQuality);
        VodComposeHostKt.setVodFlags(mBinding.flagCompose, mSession, this::onComposeFlag);
        VodComposeHostKt.setVodQualities(mBinding.qualityCompose, mSession, this::onComposeQuality);
        VodComposeHostKt.setVodEpisodes(mBinding.episodeCompose, mSession, this::onComposeEpisode);
        VodSheetsKt.setVodSheetHost(mBinding.sheetCompose, mSession, sheetController, this::onSheetPlayEpisode, this::onSheetDownloadEpisode, this::onSheetDownloadList, this::onSheetDownloadConfirm, this::onSheetInfoShare);
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
        setText(mBinding.site, R.string.detail_site, siteRaw);
        setText(mBinding.director, R.string.detail_director, item.getDirector());
        setText(mBinding.actor, R.string.detail_actor, item.getActor());
        setText(mBinding.content, 0, item.getContent());
        setText(mBinding.remark, 0, remarkRaw);
        hideDetailHeaderXml();
        String siteText = TextUtils.isEmpty(siteRaw) ? null : mBinding.site.getText().toString();
        String remarkText = TextUtils.isEmpty(remarkRaw) ? null : mBinding.remark.getText().toString();
        updateDetailMeta(mBinding.name.getText().toString(), siteText, remarkText);
        setOther(mBinding.other, item);
    }

    void updateDetailMeta(String title, String siteName, String remark) {
        if (mSession != null) mSession.updateDetailMeta(title, siteName, remark);
    }

    void hideDetailHeaderXml() {
        mBinding.name.setVisibility(View.GONE);
        mBinding.remark.setVisibility(View.GONE);
        mBinding.site.setVisibility(View.GONE);
    }

    void setText(TextView view, int resId, String text) {
        if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(view.getText())) return;
        view.setText(Sniffer.buildClickable(resId > 0 ? getString(resId, text) : text, this::clickableSpan), TextView.BufferType.SPANNABLE);
        view.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        if (view == mBinding.content) setContentVisible();
        view.setLinkTextColor(Color.WHITE);
        CustomMovement.bind(view);
    }

    private void setContentVisible() {
        mBinding.contentLayout.setVisibility(mBinding.content.getVisibility());
    }

    private ClickableSpan clickableSpan(Result result) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                FolderActivity.start(getActivity(), getKey(), result);
                ((TextView) view).setMaxLines(Integer.MAX_VALUE);
                setRedirect(true);
            }
        };
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

    private void setOther(TextView view, Vod item) {
        StringBuilder sb = new StringBuilder();
        if (!item.getYear().isEmpty()) sb.append(getString(R.string.detail_year, item.getYear())).append("  ");
        if (!item.getArea().isEmpty()) sb.append(getString(R.string.detail_area, item.getArea())).append("  ");
        if (!item.getTypeName().isEmpty()) sb.append(getString(R.string.detail_type, item.getTypeName())).append("  ");
        view.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
        view.setText(Util.substring(sb.toString(), 2));
    }

    @Override
    public void onItemClick(Flag item) {
        mVod.selectFlag(item);
    }

    @Override
    public void onItemClick(Episode item) {
        if (shouldEnterFullscreen(item)) return;
        mVod.selectEpisode(item);
    }

    @Override
    public void onItemClick(Result result) {
        mVod.selectQuality(result);
        syncCatalog();
    }

    private void onComposeFlag(int index) {
        if (index < 0 || index >= mFlagAdapter.getItemCount()) return;
        onItemClick(mFlagAdapter.get(index));
    }

    private void onComposeEpisode(int index) {
        if (index < 0 || index >= mEpisodeAdapter.getItemCount()) return;
        onItemClick(mEpisodeAdapter.getItems().get(index));
    }

    private void onComposeQuality(int index) {
        mQualityAdapter.select(index);
    }

    void syncCatalog() {
        if (mSession == null) return;
        mSession.refresh();
    }

    @Override
    public void onItemClick(Vod item) {
        mVod.selectSource(item);
    }

    @Override
    public void setEpisodeAdapter(List<Episode> items) {
        mBinding.reverse.setVisibility(items.size() < 2 ? View.GONE : View.VISIBLE);
        mBinding.episode.setVisibility(View.GONE);
        mBinding.more.setVisibility(items.size() < 10 ? View.GONE : View.VISIBLE);
        mEpisodeAdapter.addAll(items);
        syncCatalog();
    }

    @Override
    public void setQualityVisible(boolean visible) {
        mBinding.qualityText.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBinding.quality.setVisibility(View.GONE);
        syncCatalog();
    }

    private void onName() {
        String name = mBinding.name.getText().toString();
        if (name.isEmpty()) {
            String title = mSession.getUiState().getValue().getTitle();
            if (!title.isEmpty()) name = title;
        }
        Notify.show(getString(R.string.detail_search, name));
        mVod.search(name, false);
    }

    private void onMore() {
        syncCatalog();
        ensureSheetHost();
        sheetController.showEpisodes(false);
    }

    private void onActor() {
        mBinding.actor.setMaxLines(mBinding.actor.getMaxLines() == 1 ? Integer.MAX_VALUE : 1);
    }

    private void onDirector() {
        mBinding.director.setMaxLines(mBinding.director.getMaxLines() == 1 ? Integer.MAX_VALUE : 1);
    }

    private void onContent() {
        mBinding.content.setMaxLines(mBinding.content.getMaxLines() == 3 ? Integer.MAX_VALUE : 3);
    }

    private void onReverse() {
        mVod.setRevSort(!mHistory.isRevSort());
        mVod.reverseEpisode(false);
    }

    private boolean onChange() {
        mVod.manualSwitchSource();
        return true;
    }

    private boolean onCopy() {
        Util.copy(mBinding.content.getText().toString());
        return true;
    }

    private void onBack() {
        if (isFullscreen()) exitFullscreen();
        else finish();
    }

    private void onCast() {
        CastDialog.create().history(mHistory).video(new CastVideo(mBinding.name.getText().toString(), player().getUrl(), player().getPosition(), player().getHeaders())).fm(true).show(this);
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
        ControlDialog.create().parse(isUseParse()).player(player()).show(this);
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

    @Override
    public void onTrack(int type) {
        TrackDialog.create().type(type).player(player()).show(this);
        hideControl();
    }

    @Override
    public void onEdition() {
        EditionDialog.create().player(player()).show(this);
        hideControl();
    }

    @Override
    public void onChapter() {
        ChapterDialog.create().player(player()).show(this);
        hideControl();
    }

    @Override
    public void onDanmaku() {
        DanmakuDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onDanmakuShow() {
        DanmakuSetting.putShow(!DanmakuSetting.isShow());
        checkDanmakuImg();
        syncDanmakuEnabled();
    }

    @Override
    public void onRepeat() {
        player().setRepeatOne(!player().isRepeatOne());
    }

    @Override
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

    @Override
    public void onDecode() {
        mClock.setCallback(null);
        PlaybackAction.toggleDecode(player());
        setR1Callback();
    }

    @Override
    public void onEnding() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetEnding(position, duration)) setEnding(duration - position);
        setR1Callback();
    }

    @Override
    public boolean onEndingReset() {
        setR1Callback();
        setEnding(0);
        return true;
    }

    private void setEnding(long ending) {
        mVod.setEnding(ending);
    }

    @Override
    public void onOpening() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetOpening(position, duration)) setOpening(position);
        setR1Callback();
    }

    @Override
    public boolean onOpeningReset() {
        setR1Callback();
        setOpening(0);
        return true;
    }

    private void setOpening(long opening) {
        mVod.setOpening(opening);
    }

    @Override
    public String openingLabel() {
        if (mHistory == null || mHistory.getOpening() <= 0) return getString(R.string.play_op);
        return Util.timeMs(mHistory.getOpening());
    }

    @Override
    public String endingLabel() {
        if (mHistory == null || mHistory.getEnding() <= 0) return getString(R.string.play_ed);
        return Util.timeMs(mHistory.getEnding());
    }

    @Override
    public void onChoose() {
        PlayerEngineDialog.show(this, null, player(), playTitle);
        hideControl();
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
        mKeyDown.resetScale();
        App.post(mR3, 2000);
        hideControl();
    }

    private void exitFullscreen() {
        if (!isFullscreen()) return;
        setFullscreen(false);
        if (isLand() && !player().isPortrait()) setTransition();
        setRequestedOrientation(PlaybackOrientation.getExitFullscreenOrientation(isPort()));
        mBinding.episode.postDelayed(() -> mBinding.episode.scrollToPosition(mEpisodeAdapter.getPosition()), 100);
        mBinding.video.setLayoutParams(mFrameParams);
        mKeyDown.resetScale();
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
        mBinding.widget.error.setVisibility(View.GONE);
        if (mSession != null) mSession.setErrorMessage(text);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.error.setText("");
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
        syncComposeChrome(true);
        updatePlayInfo();
        setR1Callback();
    }

    private void hideControl() {
        setComposeControlVisible(false);
        syncComposeChrome(false);
        updatePlayInfo();
        App.removeCallbacks(mR1);
    }

    private void updatePlayInfo() {
        mBinding.widget.playInfo.setVisibility(View.GONE);
        boolean fullscreen = isFullscreen() && !isInPictureInPictureMode();
        if (fullscreen && !isRotate()) setPadding(mBinding.gestureCompose);
        else noPadding(mBinding.gestureCompose);
        if (mSession != null) {
            syncComposeChrome(isControlOpen());
        }
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
        if (name) mBinding.name.setText(item.getName());
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
                mClock.setCallback(null);
                break;
            case Player.STATE_READY:
                hideProgress();
                checkControl();
                player().reset();
                mClock.setCallback(this);
                break;
            case Player.STATE_ENDED:
                hideProgress();
                setR1Callback();
                mClock.setCallback(null);
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

    void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        view.post(() -> adapter.notifyItemRangeChanged(0, adapter.getItemCount()));
    }

    void scrollToPosition(RecyclerView view, int position) {
        view.post(() -> view.scrollToPosition(position));
    }

    @Override
    public void onCasted() {
        player().stop();
    }

    @Override
    public void onScale(int tag) {
        mKeyDown.resetScale();
        setScale(tag);
    }

    @Override
    public void onParse(Parse item) {
        mVod.selectParse(item);
    }

    @Override
    public void onSpeedUp() {
        if (!player().isPlaying()) return;
        mBinding.widget.speed.setVisibility(View.GONE);
        mSession.showSpeedHud();
        PlaybackAction.setSpeed(player(), PlayerSetting.getSpeed());
    }

    @Override
    public void onSpeedEnd() {
        mBinding.widget.speed.setVisibility(View.GONE);
        mSession.clearGestureHud();
        PlaybackAction.setSpeed(player(), mHistory.getSpeed());
    }

    @Override
    public void onBright(int progress) {
        mBinding.widget.bright.setVisibility(View.GONE);
        mSession.updateGestureHud(true, progress, false, 0);
    }

    @Override
    public void onVolume(int progress) {
        mBinding.widget.volume.setVisibility(View.GONE);
        mSession.updateGestureHud(false, 0, true, progress);
    }

    @Override
    public void onFlingUp() {
        if (mEpisodeAdapter.getItemCount() == 1) onRefresh();
        else mVod.nextEpisode(true);
    }

    @Override
    public void onFlingDown() {
        if (mEpisodeAdapter.getItemCount() == 1) onRefresh();
        else mVod.prevEpisode(true);
    }

    @Override
    public void onSeeking(long time) {
        mBinding.widget.seek.setVisibility(View.GONE);
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
        mBinding.widget.seek.setVisibility(View.GONE);
        mBinding.widget.speed.setVisibility(View.GONE);
        mBinding.widget.bright.setVisibility(View.GONE);
        mBinding.widget.volume.setVisibility(View.GONE);
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
        mKeyDown.applyScale(factor, focusX, focusY);
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
        if (mEpisodeAdapter.getItemCount() == 0) return;
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
        if (index < 0 || index >= mEpisodeAdapter.getItemCount()) return;
        onDownloadEpisode(mEpisodeAdapter.getItems().get(index));
    }

    private void onSheetDownloadList() {
        DownloadDialog.openList(this);
    }

    private void onSheetDownloadConfirm(VodDownloadConfirm confirm) {
        if (DownloadDialog.send(this, confirm.getFileName(), confirm.getUrl(), confirm.getHeaders())) {
            sheetController.dismiss();
        }
    }

    private void onSheetInfoShare(String title) {
        onShare(title);
    }

    private void onDownloadEpisode(Episode item) {
        int seq = ++downloadSeq;
        stopDownloadParse();
        if (item.equals(mEpisodeAdapter.getActivated())) {
            DownloadResolveOutcome.Ready ready = mSession.prepareDownloadFromPlayer(
                    player().getUrl(),
                    player().getHeaders(),
                    VodDownloadResolver.buildFileName(mBinding.name.getText().toString(), item.getName())
            );
            if (ready != null) {
                showDownload(item, ready.getUrl(), ready.getHeaders());
                return;
            }
        }
        Flag flag = mFlagAdapter.getSelected();
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
                            VodDownloadResolver.buildFileName(mBinding.name.getText().toString(), item.getName())
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
        String save = VodDownloadResolver.buildFileName(mBinding.name.getText().toString(), item == null ? "" : item.getName());
        long duration = item != null && item.equals(mEpisodeAdapter.getActivated()) ? player().getDuration() : 0;
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
        mClock.stop().start();
        setAudioOnly(false);
        setStop(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (PlayerSetting.isBackgroundOff()) mClock.stop();
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
        mClock.release();
        saveHistory(true);
        Timer.get().reset();
        DanmakuApi.cancel();
        RefreshEvent.keep();
        App.removeCallbacks(mR1, mR2, mR3, mR4);
        super.onDestroy();
    }
}
