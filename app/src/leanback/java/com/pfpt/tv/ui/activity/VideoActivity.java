package com.pfpt.tv.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.OnChildViewHolderSelectedListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.VideoSize;
import androidx.media3.ui.PlayerSeekView;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.bumptech.glide.request.transition.Transition;
import com.pfpt.tv.App;
import com.pfpt.tv.Constant;
import com.pfpt.tv.R;
import com.pfpt.tv.api.DanmakuApi;
import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.Danmaku;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Keep;
import com.pfpt.tv.bean.Parse;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.bean.Site;
import com.pfpt.tv.bean.Sub;
import com.pfpt.tv.bean.Vod;
import com.pfpt.tv.databinding.ActivityVideoBinding;
import com.pfpt.tv.event.RefreshEvent;
import com.pfpt.tv.impl.CustomTarget;
import com.pfpt.tv.model.VideoViewModel;
import com.pfpt.tv.player.util.PlayerHelper;
import com.pfpt.tv.service.PlaybackService;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.setting.PlayerSetting;
import com.pfpt.tv.ui.adapter.ArrayAdapter;
import com.pfpt.tv.ui.adapter.EpisodeAdapter;
import com.pfpt.tv.ui.adapter.FlagAdapter;
import com.pfpt.tv.ui.adapter.PartAdapter;
import com.pfpt.tv.ui.adapter.QualityAdapter;
import com.pfpt.tv.ui.adapter.QuickAdapter;
import com.pfpt.tv.ui.custom.CustomKeyDownVod;
import com.pfpt.tv.ui.custom.CustomMovement;
import com.pfpt.tv.ui.dialog.ChapterDialog;
import com.pfpt.tv.ui.dialog.ContentDialog;
import com.pfpt.tv.ui.dialog.DanmakuDialog;
import com.pfpt.tv.ui.dialog.EditionDialog;
import com.pfpt.tv.ui.dialog.ParseDialog;
import com.pfpt.tv.ui.dialog.PlayerEngineDialog;
import com.pfpt.tv.ui.dialog.SubtitleDialog;
import com.pfpt.tv.ui.dialog.TrackDialog;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.vod.VodLaunch;
import com.pfpt.tv.playback.vod.VodPlaybackActivityAccess;
import com.pfpt.tv.playback.vod.VodPlaybackController;
import com.pfpt.tv.playback.vod.VodPlaybackMedia;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.playback.vod.session.VodSessionViewModel;
import com.pfpt.tv.playback.vod.session.VodUiState;
import com.pfpt.tv.utils.Clock;
import com.pfpt.tv.utils.ImgUtil;
import com.pfpt.tv.utils.KeyUtil;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.PartUtil;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Sniffer;
import com.pfpt.tv.utils.Task;
import com.pfpt.tv.utils.Traffic;
import com.pfpt.tv.utils.Util;
import com.github.bassaer.library.MDColor;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VideoActivity extends PlaybackActivity implements VodPlaybackActivityAccess, CustomKeyDownVod.Listener, TrackDialog.Listener, ParseDialog.Listener, ArrayAdapter.OnClickListener, FlagAdapter.OnClickListener, EpisodeAdapter.OnClickListener, QualityAdapter.OnClickListener, QuickAdapter.OnClickListener, Clock.Callback {

    ActivityVideoBinding mBinding;
    private ViewGroup.LayoutParams mFrameParams;
    private Observer<Result> mObserveDetail;
    private Observer<Result> mObservePlayer;
    private Observer<Result> mObserveSearch;
    EpisodeAdapter mEpisodeAdapter;
    QualityAdapter mQualityAdapter;
    private ArrayAdapter mArrayAdapter;
    QuickAdapter mQuickAdapter;
    FlagAdapter mFlagAdapter;
    private PartAdapter mPartAdapter;
    private VodPlaybackController mVod;
    private CustomKeyDownVod mKeyDown;
    VideoViewModel mViewModel;
    private VodSessionViewModel mSession;
    History mHistory;
    private boolean fullscreen;
    private boolean useParse;
    private Runnable mR1;
    private Runnable mR2;
    private Runnable mR3;
    Runnable mR4;
    Clock mClock;
    private final LeanbackSpanControls spanControls = new LeanbackSpanControls();
    private final LeanbackVodCatalog catalog = new LeanbackVodCatalog(new CatalogHost());
    private View mFocus1;
    private View mFocus2;

    public static void push(FragmentActivity activity, String text) {
        VodLaunch.push(activity, VideoActivity.class, text);
    }

    public static void file(FragmentActivity activity, String path) {
        VodLaunch.file(activity, VideoActivity.class, path);
    }

    public static void cast(Activity activity, History history) {
        VodLaunch.cast(activity, VideoActivity.class, history);
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

    public static void start(Activity activity, String key, String id, String name, String pic, String mark, boolean collect, boolean cast) {
        VodLaunch.start(activity, VideoActivity.class, key, id, name, pic, mark, collect, cast);
    }

    private boolean isCast() {
        return VodLaunch.flag(getIntent(), "cast");
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
        return mEpisodeAdapter.getActivated();
    }

    int getScale() {
        return History.resolveScale(mHistory);
    }

    void setScale(int scale) {
        mVod.setScale(scale);
        mBinding.player.setResizeMode(scale);
        mBinding.control.action.scale.setText(ResUtil.getStringArray(R.array.select_scale)[scale]);
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
        return mBinding.control.seek;
    }

    @Override
    protected void onServiceConnected() {
        checkId();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String oldId = getId();
        super.onNewIntent(intent);
        String id = Objects.toString(intent.getStringExtra("id"), "");
        if (TextUtils.isEmpty(id) || id.equals(oldId)) return;
        saveHistory(false);
        getIntent().putExtras(intent);
        mVod.reset();
        checkId();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mFrameParams = mBinding.video.getLayoutParams();
        mClock = Clock.create(mBinding.widget.clock);
        mKeyDown = CustomKeyDownVod.create(this);
        mObserveDetail = this::onDetailObserved;
        mObservePlayer = this::onPlayerObserved;
        mObserveSearch = this::onSearchObserved;
        mR1 = this::hideControl;
        mR2 = this::updateFocus;
        mR3 = this::setTraffic;
        mR4 = this::showEmpty;
        setRecyclerView();
        setVideoView();
        setViewModel();
        checkCast();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initEvent() {
        mBinding.keep.setOnClickListener(view -> onKeep());
        mBinding.video.setOnClickListener(view -> onVideo());
        mBinding.change.setOnClickListener(view -> onChange());
        mBinding.content.setOnClickListener(view -> onContent());
        mBinding.control.action.text.setOnClickListener(this::onTrack);
        mBinding.control.action.audio.setOnClickListener(this::onTrack);
        mBinding.control.action.video.setOnClickListener(this::onTrack);
        mBinding.control.action.speed.setUpListener(this::onSpeedAdd);
        mBinding.control.action.speed.setDownListener(this::onSpeedSub);
        mBinding.control.action.ending.setUpListener(this::onEndingAdd);
        mBinding.control.action.ending.setDownListener(this::onEndingSub);
        mBinding.control.action.opening.setUpListener(this::onOpeningAdd);
        mBinding.control.action.opening.setDownListener(this::onOpeningSub);
        mBinding.control.action.text.setUpListener(this::onSubtitleClick);
        mBinding.control.action.text.setDownListener(this::onSubtitleClick);
        mBinding.control.action.next.setOnClickListener(view -> checkNext());
        mBinding.control.action.prev.setOnClickListener(view -> checkPrev());
        mBinding.control.action.scale.setOnClickListener(view -> onScale());
        mBinding.control.action.speed.setOnClickListener(view -> onSpeed());
        mBinding.control.action.reset.setOnClickListener(view -> onReset());
        mBinding.control.action.replay.setOnClickListener(view -> onReplay());
        mBinding.control.action.parse.setOnClickListener(view -> onParse());
        mBinding.control.action.player.setOnClickListener(view -> onChoose());
        mBinding.control.action.decode.setOnClickListener(view -> onDecode());
        mBinding.control.action.ending.setOnClickListener(view -> onEnding());
        mBinding.control.action.repeat.setOnClickListener(view -> onRepeat());
        mBinding.control.action.danmaku.setOnClickListener(view -> onDanmaku());
        mBinding.control.action.edition.setOnClickListener(view -> onEdition());
        mBinding.control.action.chapter.setOnClickListener(view -> onChapter());
        mBinding.control.action.opening.setOnClickListener(view -> onOpening());
        mBinding.control.action.ending.setOnLongClickListener(view -> onEndingReset());
        mBinding.control.action.opening.setOnLongClickListener(view -> onOpeningReset());
        mBinding.video.setOnTouchListener((view, event) -> mKeyDown.onTouchEvent(event));
        mBinding.flag.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mFlagAdapter.getItemCount() > 0) onItemClick(mFlagAdapter.get(position));
            }
        });
        mBinding.episode.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (child != null && mBinding.video != mFocus1) mFocus1 = child.itemView;
            }
        });
        mBinding.array.addOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(@NonNull RecyclerView parent, @Nullable RecyclerView.ViewHolder child, int position, int subposition) {
                if (mEpisodeAdapter.getItemCount() > 20 && position > 1) mBinding.episode.setSelectedPosition((position - 2) * 20);
            }
        });
    }

    private void setRecyclerView() {
        catalog.bind();
    }

    private void setVideoView() {
        setSeekNextFocusDown(R.id.next);
        setActionFocusBoundary(mBinding.control.action.getRoot());
        PlayerEngineDialog.setText(mBinding.control.action.player);
        mBinding.control.action.danmaku.setVisibility(DanmakuSetting.isLoad() ? View.VISIBLE : View.GONE);
    }

    private void setPlaybackMode() {
        PlaybackAction.setPlaybackMode(player(), mBinding.control.action.player, mBinding.control.action.decode);
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        mSession = new ViewModelProvider(this).get(VodSessionViewModel.class);
        mSession.bind(mViewModel.getPlaybackState());
        mSession.getUiStateLiveData().observe(this, this::onSessionState);
        observeForever(mViewModel.getResult(), mObserveDetail);
        observeForever(mViewModel.getPlayer(), mObservePlayer);
        observeForever(mViewModel.getSearch(), mObserveSearch);
        mVod = mViewModel.createPlaybackController(new VodPlaybackBinder(this));
    }

    private void onSessionState(VodUiState state) {
        if (state == null || isFinishing() || isDestroyed()) return;
        mBinding.keep.setCompoundDrawablesWithIntrinsicBounds(state.getKept() ? R.drawable.ic_detail_keep_on : R.drawable.ic_detail_keep_off, 0, 0, 0);
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

    private void checkCast() {
        if (isCast() && !isFullscreen()) enterFullscreen();
        else mBinding.progressLayout.showProgress();
    }

    private void checkId() {
        mVod.checkId();
    }

    @Override
    public void showEmpty() {
        mBinding.progressLayout.showEmpty();
    }

    void setText(Vod item) {
        mBinding.content.setTag(item.getContent());
        setText(mBinding.year, R.string.detail_year, item.getYear());
        setText(mBinding.area, R.string.detail_area, item.getArea());
        setText(mBinding.type, R.string.detail_type, item.getTypeName());
        setText(mBinding.site, R.string.detail_site, getSite().getName());
        setText(mBinding.director, R.string.detail_director, item.getDirector());
        setText(mBinding.actor, R.string.detail_actor, item.getActor());
        setText(mBinding.remark, 0, item.getRemarks());
    }

    private void setText(TextView view, int resId, String text) {
        if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(view.getText())) return;
        view.setText(Sniffer.buildClickable(resId > 0 ? getString(resId, text) : text, this::clickableSpan), TextView.BufferType.SPANNABLE);
        view.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        view.setLinkTextColor(MDColor.YELLOW_500);
        CustomMovement.bind(view);
    }

    private ClickableSpan clickableSpan(Result result) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                VodActivity.start(getActivity(), getKey(), result);
                setRedirect(true);
            }
        };
    }

    @Override
    public void onItemClick(Flag item) {
        catalog.onFlag(item);
    }

    @Override
    public void setEpisodeAdapter(List<Episode> items) {
        catalog.setEpisodes(items);
    }

    @Override
    public void onItemClick(Episode item) {
        catalog.onEpisode(item);
    }

    @Override
    public void setQualityVisible(boolean visible) {
        catalog.setQualityVisible(visible);
    }

    @Override
    public void onItemClick(Result result) {
        catalog.onQuality(result);
    }

    private void updateFocus() {
        catalog.updateFocus();
    }

    @Override
    public void onRevSort() {
        mVod.setRevSort(!mHistory.isRevSort());
        mVod.reverseEpisode(false);
    }

    @Override
    public void onRevPlay(TextView view) {
        mVod.setRevPlay(!mHistory.isRevPlay());
        view.setText(mHistory.getRevPlayText());
        Notify.show(mHistory.getRevPlayHint());
    }

    private void enterFullscreen() {
        mFocus1 = getCurrentFocus();
        mBinding.video.requestFocus();
        mBinding.video.setForeground(null);
        mBinding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mBinding.flag.setSelectedPosition(mFlagAdapter.getPosition());
        mKeyDown.setFull(true);
        setFullscreen(true);
        mFocus2 = null;
    }

    private void exitFullscreen() {
        mBinding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video));
        mBinding.video.setLayoutParams(mFrameParams);
        getFocus1().requestFocus();
        mKeyDown.setFull(false);
        setFullscreen(false);
        mFocus2 = null;
        hideInfo();
    }

    private void onContent() {
        if (mBinding.content.getTag() == null) return;
        ContentDialog.create().content(mBinding.content.getTag().toString()).show(this);
    }

    private void onKeep() {
        if (mHistory == null || mSession == null) return;
        mSession.toggleKeep(getHistoryKey(), VodConfig.getCid(), mHistory.getVodName(), mHistory.getVodPic(), getSite().getName(), (kept, removed) -> {
            if (isFinishing() || isDestroyed()) return;
            Notify.show(removed ? R.string.keep_del : R.string.keep_add);
        });
    }

    private void onVideo() {
        if (!isFullscreen()) enterFullscreen();
    }

    private void onChange() {
        mVod.manualSwitchSource();
    }

    private void onRepeat() {
        player().setRepeatOne(!player().isRepeatOne());
        mBinding.control.action.repeat.setSelected(player().isRepeatOne());
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        mBinding.control.action.repeat.setSelected(player().isRepeatOne());
    }

    private void checkNext() {
        catalog.checkNext();
    }

    private void checkNext(boolean notify) {
        catalog.checkNext(notify);
    }

    private void checkPrev() {
        catalog.checkPrev();
    }

    private void onScale() {
        int index = getScale();
        String[] array = ResUtil.getStringArray(R.array.select_scale);
        setScale(PlayerSetting.nextScale(index, array.length));
    }

    private void onSpeed() {
        spanControls.speed(mVod, player(), mBinding.control.action.speed);
    }

    private void onSpeedAdd() {
        spanControls.speedAdd(mVod, player(), mBinding.control.action.speed);
    }

    private void onSpeedSub() {
        spanControls.speedSub(mVod, player(), mBinding.control.action.speed);
    }

    private void onReset() {
        onRefresh();
    }

    private void onParse() {
        ParseDialog.create().show(this);
        hideControl();
    }

    private void onReplay() {
        mVod.replay();
    }

    private void onRefresh() {
        mVod.refresh();
    }

    private void onOpening() {
        spanControls.opening(mVod, player(), mHistory, mBinding.control.action.opening);
    }

    private void onOpeningAdd() {
        spanControls.openingAdd(mVod, mHistory, mBinding.control.action.opening);
    }

    private void onOpeningSub() {
        spanControls.openingSub(mVod, mHistory, mBinding.control.action.opening);
    }

    private boolean onOpeningReset() {
        spanControls.openingReset(mVod, mHistory, mBinding.control.action.opening);
        return true;
    }

    private void onEnding() {
        spanControls.ending(mVod, player(), mHistory, mBinding.control.action.ending);
    }

    private void onEndingAdd() {
        spanControls.endingAdd(mVod, mHistory, mBinding.control.action.ending);
    }

    private void onEndingSub() {
        spanControls.endingSub(mVod, mHistory, mBinding.control.action.ending);
    }

    private boolean onEndingReset() {
        spanControls.endingReset(mVod, mHistory, mBinding.control.action.ending);
        return true;
    }

    private void onChoose() {
        PlayerEngineDialog.show(this, mBinding.control.action.player, player(), mBinding.widget.title.getText());
        hideControl();
    }

    private void onDecode() {
        mClock.setCallback(null);
        PlaybackAction.toggleDecode(player());
    }

    private void onTrack(View view) {
        TrackDialog.create().type(Integer.parseInt(view.getTag().toString())).player(player()).show(this);
        hideControl();
    }

    private void onEdition() {
        EditionDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onChapter() {
        ChapterDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onDanmaku() {
        DanmakuDialog.create().player(player()).show(this);
        hideControl();
    }

    private void onToggle() {
        if (isVisible(mBinding.control.getRoot())) hideControl();
        else showControl(getFocus2());
    }

    void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(mR3, 0);
        hideCenter();
        hideError();
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR3);
        Traffic.reset();
    }

    void showError(String text) {
        mBinding.widget.error.setVisibility(View.VISIBLE);
        mBinding.widget.text.setText(text);
        hideProgress();
    }

    private void hideError() {
        mBinding.widget.error.setVisibility(View.GONE);
        mBinding.widget.text.setText("");
    }

    private void showInfo() {
        mBinding.widget.top.setVisibility(View.VISIBLE);
        mBinding.widget.center.setVisibility(View.VISIBLE);
        mBinding.widget.duration.setText(player().getDurationTime());
        mBinding.widget.position.setText(player().getPositionTime(0));
    }

    private void hideInfo() {
        mBinding.widget.top.setVisibility(View.GONE);
        mBinding.widget.center.setVisibility(View.GONE);
    }

    private void showControl(View view) {
        mBinding.control.getRoot().setVisibility(View.VISIBLE);
        view.requestFocus();
        setR1Callback();
    }

    private void hideControl() {
        mBinding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(mR1);
    }

    private void hideCenter() {
        mBinding.widget.action.setImageResource(R.drawable.ic_widget_play);
        hideInfo();
    }

    private void setTraffic() {
        Traffic.setSpeed(mBinding.progress.traffic);
        App.post(mR3, 1000);
    }

    private void setR1Callback() {
        if (isScrubbing()) return;
        App.post(mR1, Constant.INTERVAL_HIDE);
    }

    @Override
    protected void onScrubbingChanged(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(mR1);
        else if (isVisible(mBinding.control.getRoot())) setR1Callback();
    }

    private void setR2Callback() {
        App.post(mR2, 500);
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

    void setPartAdapter() {
        mPartAdapter.addAll(PartUtil.split(mHistory.getVodName()));
        mBinding.part.setVisibility(View.VISIBLE);
        setR2Callback();
    }

    private void saveHistory(boolean exit) {
        if (mVod == null) return;
        boolean owner = service() != null && isOwner();
        mVod.saveHistory(exit, owner, player().getPosition(), player().getDuration());
    }

    private void syncHistory() {
        if (mVod != null) mVod.syncHistory();
    }

    void checkKeepImg() {
        if (mSession == null) return;
        mSession.refresh();
        mSession.loadKept(getHistoryKey());
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
        if (name) mBinding.widget.title.setText(item.getName());
        mVod.mergeFlags(item.getFlags());
        if (pic) setArtwork(item.getPic());
        if (pic || name) setMetadata();
        if (pic || name) syncHistory();
        if (pic || name) updateKeep();
        if (id) updateNavigationKey();
        if (name) setPartAdapter();
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
    };

    @Override
    protected String getPlaybackKey() {
        return getHistoryKey();
    }

    @Override
    protected void onPrepare() {
        setPlaybackMode();
    }

    @Override
    protected void onDecodeChanged() {
        setPlaybackMode();
    }

    @Override
    protected void onTracksChanged() {
        setTrackVisible();
    }

    @Override
    protected void onMediaOptionsChanged() {
        setMediaOptionVisible();
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
                player().reset();
                mClock.setCallback(this);
                break;
            case Player.STATE_ENDED:
                hideProgress();
                mClock.setCallback(null);
                break;
        }
    }

    @Override
    protected void onPlayingChanged(boolean isPlaying) {
        if (isPlaying) {
            hideCenter();
        } else if (isPaused()) {
            if (isFullscreen()) showInfo();
            else hideInfo();
        }
    }

    @Override
    protected void onSizeChanged(VideoSize size) {
        mBinding.widget.size.setText(player().getSizeText());
    }

    @Override
    public void onSubtitleClick() {
        SubtitleDialog.create().view(mBinding.player.getSubtitleView()).player(player()).show(this);
        App.post(this::hideControl, 100);
    }

    @Override
    public void onTimeChanged(long time) {
        if (!isOwner() || !player().isVod()) return;
        long position = player().getPosition();
        long duration = player().getDuration();
        if (position < 0 || duration <= 0) return;
        mVod.onTimeChanged(time, position, duration);
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

    private void setTrackVisible() {
        PlaybackAction.setTracks(player(), mBinding.control.action.text, mBinding.control.action.audio, mBinding.control.action.video);
    }

    private void setMediaOptionVisible() {
        PlaybackAction.setMediaOptions(player(), mBinding.control.action.edition, mBinding.control.action.chapter);
    }

    private MediaMetadata buildMetadata() {
        return VodPlaybackMedia.metadata(mHistory, getEpisode());
    }

    private void setMetadata() {
        player().setMetadata(buildMetadata());
    }

    @Override
    public void onItemClick(Vod item) {
        mVod.selectSource(item);
    }

    @Override
    public void onParse(Parse item) {
        mVod.selectParse(item);
    }

    private void onPaused() {
        controller().pause();
    }

    private void onPlay() {
        if (mHistory != null && isEnded()) controller().seekTo(mHistory.getOpening());
        if (!player().isEmpty() && isIdle()) controller().prepare();
        controller().play();
    }

    private boolean onSeekBack() {
        controller().seekBack();
        return true;
    }

    private boolean onSeekForward() {
        controller().seekForward();
        return true;
    }

    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    private void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public void notifyItemChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
        super.notifyItemChanged(view, adapter);
    }

    public boolean isUseParse() {
        return useParse;
    }

    public void setUseParse(boolean useParse) {
        this.useParse = useParse;
    }

    private View getFocus1() {
        return mFocus1 == null || mFocus1.getVisibility() != View.VISIBLE ? mBinding.video : mFocus1;
    }

    private View getFocus2() {
        return mFocus2 == null || mFocus2.getVisibility() != View.VISIBLE || mFocus2 == mBinding.control.action.opening || mFocus2 == mBinding.control.action.ending ? mBinding.control.action.next : mFocus2;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isFullscreen() && KeyUtil.isMenuKey(event)) onToggle();
        if (isVisible(mBinding.control.getRoot())) setR1Callback();
        if (isVisible(mBinding.control.getRoot())) mFocus2 = getCurrentFocus();
        if (isFullscreen() && isGone(mBinding.control.getRoot()) && mKeyDown.hasEvent(event) && service() != null) return mKeyDown.onKeyDown(event);
        if (KeyUtil.isMediaFastForward(event)) return onSeekForward();
        if (KeyUtil.isMediaRewind(event)) return onSeekBack();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onSeeking(long time) {
        mBinding.widget.center.setVisibility(View.VISIBLE);
        mBinding.widget.duration.setText(player().getDurationTime());
        mBinding.widget.position.setText(player().getPositionTime(time));
        mBinding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        hideProgress();
    }

    @Override
    public void onSeekEnd(long time) {
        mKeyDown.reset();
        seekTo(time);
    }

    @Override
    public void onSpeedUp() {
        if (!player().isPlaying()) return;
        mBinding.widget.speed.setVisibility(View.VISIBLE);
        mBinding.widget.speed.startAnimation(ResUtil.getAnim(R.anim.forward));
        PlaybackAction.setSpeed(player(), mBinding.control.action.speed, PlayerSetting.getSpeed());
    }

    @Override
    public void onSpeedEnd() {
        mBinding.widget.speed.clearAnimation();
        mBinding.widget.speed.setVisibility(View.GONE);
        PlaybackAction.setSpeed(player(), mBinding.control.action.speed, mHistory.getSpeed());
    }

    @Override
    public void onKeyUp() {
        long position = player().getPosition();
        long duration = player().getDuration();
        if (player().canSetOpening(position, duration)) {
            showControl(mBinding.control.action.opening);
        } else if (player().canSetEnding(position, duration)) {
            showControl(mBinding.control.action.ending);
        } else {
            showControl(getFocus2());
        }
    }

    @Override
    public void onKeyDown() {
        showControl(getFocus2());
    }

    @Override
    public void onKeyCenter() {
        if (player().isPlaying()) onPaused();
        else if (player().isEmpty()) onRefresh();
        else onPlay();
        hideControl();
    }

    @Override
    public void onSingleTap() {
        if (isFullscreen()) onToggle();
    }

    @Override
    public void onDoubleTap() {
        if (isFullscreen()) onKeyCenter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1001) PlayerHelper.onExternalResult(data, service()::dispatchNext, controller()::seekTo);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClock.stop().start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (PlayerSetting.isBackgroundOff()) mClock.stop();
    }

    @Override
    protected void onBackInvoked() {
        if (isVisible(mBinding.control.getRoot())) {
            hideControl();
        } else if (isVisible(mBinding.widget.center)) {
            hideCenter();
        } else if (isFullscreen()) {
            exitFullscreen();
        } else {
            mViewModel.stopSearch();
            if (isTaskRoot()) startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            super.onBackInvoked();
        }
    }

    @Override
    protected void onDestroy() {
        mClock.release();
        saveHistory(true);
        DanmakuApi.cancel();
        RefreshEvent.keep();
        App.removeCallbacks(mR1, mR2, mR3, mR4);
        super.onDestroy();
    }

    private final class CatalogHost implements LeanbackVodCatalog.Host {
        @Override
        public ActivityVideoBinding binding() {
            return mBinding;
        }

        @Override
        public void setFlagAdapter(FlagAdapter adapter) {
            mFlagAdapter = adapter;
        }

        @Override
        public FlagAdapter flagAdapter() {
            return mFlagAdapter;
        }

        @Override
        public void setEpisodeAdapter(EpisodeAdapter adapter) {
            mEpisodeAdapter = adapter;
        }

        @Override
        public EpisodeAdapter episodeAdapter() {
            return mEpisodeAdapter;
        }

        @Override
        public void setQualityAdapter(QualityAdapter adapter) {
            mQualityAdapter = adapter;
        }

        @Override
        public QualityAdapter qualityAdapter() {
            return mQualityAdapter;
        }

        @Override
        public void setArrayAdapter(ArrayAdapter adapter) {
            mArrayAdapter = adapter;
        }

        @Override
        public ArrayAdapter arrayAdapter() {
            return mArrayAdapter;
        }

        @Override
        public void setPartAdapter(PartAdapter adapter) {
            mPartAdapter = adapter;
        }

        @Override
        public PartAdapter partAdapter() {
            return mPartAdapter;
        }

        @Override
        public void setQuickAdapter(QuickAdapter adapter) {
            mQuickAdapter = adapter;
        }

        @Override
        public VodPlaybackController vod() {
            return mVod;
        }

        @Override
        public History history() {
            return mHistory;
        }

        @Override
        public boolean fullscreen() {
            return isFullscreen();
        }

        @Override
        public void enterFullscreen() {
            VideoActivity.this.enterFullscreen();
        }

        @Override
        public boolean visible(View view) {
            return isVisible(view);
        }

        @Override
        public View find(int id) {
            return findViewById(id);
        }

        @Override
        public void notifyChanged(RecyclerView view, RecyclerView.Adapter<?> adapter) {
            notifyItemChanged(view, adapter);
        }

        @Override
        public void postFocus() {
            setR2Callback();
        }

        @Override
        public String text(int resId) {
            return getString(resId);
        }

        @Override
        public FlagAdapter.OnClickListener flagClicks() {
            return VideoActivity.this;
        }

        @Override
        public EpisodeAdapter.OnClickListener episodeClicks() {
            return VideoActivity.this;
        }

        @Override
        public QualityAdapter.OnClickListener qualityClicks() {
            return VideoActivity.this;
        }

        @Override
        public ArrayAdapter.OnClickListener arrayClicks() {
            return VideoActivity.this;
        }

        @Override
        public QuickAdapter.OnClickListener quickClicks() {
            return VideoActivity.this;
        }

        @Override
        public void show(int resId) {
            Notify.show(resId);
        }
    }

}
