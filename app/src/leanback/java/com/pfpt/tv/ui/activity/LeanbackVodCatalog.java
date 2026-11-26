package com.pfpt.tv.ui.activity;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.pfpt.tv.R;
import com.pfpt.tv.bean.Episode;
import com.pfpt.tv.bean.Flag;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.bean.Result;
import com.pfpt.tv.databinding.ActivityVideoBinding;
import com.pfpt.tv.playback.vod.VodPlaybackController;
import com.pfpt.tv.ui.adapter.ArrayAdapter;
import com.pfpt.tv.ui.adapter.EpisodeAdapter;
import com.pfpt.tv.ui.adapter.FlagAdapter;
import com.pfpt.tv.ui.adapter.PartAdapter;
import com.pfpt.tv.ui.adapter.QualityAdapter;
import com.pfpt.tv.ui.adapter.QuickAdapter;
import com.pfpt.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class LeanbackVodCatalog {

    interface Host {
        ActivityVideoBinding binding();

        void setFlagAdapter(FlagAdapter adapter);

        FlagAdapter flagAdapter();

        void setEpisodeAdapter(EpisodeAdapter adapter);

        EpisodeAdapter episodeAdapter();

        void setQualityAdapter(QualityAdapter adapter);

        QualityAdapter qualityAdapter();

        void setArrayAdapter(ArrayAdapter adapter);

        ArrayAdapter arrayAdapter();

        void setPartAdapter(PartAdapter adapter);

        PartAdapter partAdapter();

        void setQuickAdapter(QuickAdapter adapter);

        VodPlaybackController vod();

        History history();

        boolean fullscreen();

        void enterFullscreen();

        boolean visible(View view);

        View find(int id);

        void notifyChanged(RecyclerView view, RecyclerView.Adapter<?> adapter);

        void postFocus();

        String text(int resId);

        FlagAdapter.OnClickListener flagClicks();

        EpisodeAdapter.OnClickListener episodeClicks();

        QualityAdapter.OnClickListener qualityClicks();

        ArrayAdapter.OnClickListener arrayClicks();

        QuickAdapter.OnClickListener quickClicks();

        void show(int resId);
    }

    private final Host host;

    LeanbackVodCatalog(Host host) {
        this.host = host;
    }

    void bind() {
        ActivityVideoBinding binding = host.binding();
        binding.flag.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.flag.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        FlagAdapter flags = new FlagAdapter(host.flagClicks());
        host.setFlagAdapter(flags);
        binding.flag.setAdapter(flags);
        binding.episode.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.episode.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        EpisodeAdapter episodes = new EpisodeAdapter(host.episodeClicks());
        host.setEpisodeAdapter(episodes);
        binding.episode.setAdapter(episodes);
        binding.quality.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.quality.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        QualityAdapter qualities = new QualityAdapter(host.qualityClicks());
        host.setQualityAdapter(qualities);
        binding.quality.setAdapter(qualities);
        binding.array.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.array.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayAdapter arrays = new ArrayAdapter(host.arrayClicks());
        host.setArrayAdapter(arrays);
        binding.array.setAdapter(arrays);
        binding.part.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.part.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        PartAdapter parts = new PartAdapter(item -> host.vod().search(item, false));
        host.setPartAdapter(parts);
        binding.part.setAdapter(parts);
        binding.quick.setHorizontalSpacing(ResUtil.dp2px(8));
        binding.quick.setRowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        QuickAdapter quicks = new QuickAdapter(host.quickClicks());
        host.setQuickAdapter(quicks);
        binding.quick.setAdapter(quicks);
    }

    void setEpisodes(List<Episode> items) {
        host.binding().episode.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        host.episodeAdapter().addAll(items);
        setArray(items.size());
        host.postFocus();
    }

    void onFlag(Flag item) {
        host.vod().selectFlag(item);
    }

    void onEpisode(Episode item) {
        if (shouldEnterFullscreen(item)) return;
        host.vod().selectEpisode(item);
    }

    void setQualityVisible(boolean visible) {
        host.binding().quality.setVisibility(visible ? View.VISIBLE : View.GONE);
        host.postFocus();
    }

    void onQuality(Result result) {
        host.vod().selectQuality(result);
    }

    private void setArray(int size) {
        List<String> items = new ArrayList<>();
        items.add(host.text(R.string.play_reverse));
        items.add(host.text(host.history().getRevPlayText()));
        host.binding().array.setVisibility(size > 1 ? View.VISIBLE : View.GONE);
        if (host.history().isRevSort()) for (int i = size; i > 0; i -= 20) items.add(i + "-" + Math.max(i - 19, 1));
        else for (int i = 0; i < size; i += 20) items.add((i + 1) + "-" + Math.min(i + 20, size));
        host.arrayAdapter().addAll(items);
    }

    private int findFocusDown(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episode, R.id.array, R.id.part, R.id.quick);
        for (int i = 0; i < orders.size(); i++) if (i > index) if (host.visible(host.find(orders.get(i)))) return orders.get(i);
        return 0;
    }

    private int findFocusUp(int index) {
        List<Integer> orders = Arrays.asList(R.id.flag, R.id.quality, R.id.episode, R.id.array, R.id.part, R.id.quick);
        for (int i = orders.size() - 1; i >= 0; i--) if (i < index) if (host.visible(host.find(orders.get(i)))) return orders.get(i);
        return 0;
    }

    void updateFocus() {
        ActivityVideoBinding binding = host.binding();
        host.partAdapter().setNextFocusUp(findFocusUp(4));
        host.episodeAdapter().setNextFocusUp(findFocusUp(2));
        host.flagAdapter().setNextFocusDown(findFocusDown(0));
        host.episodeAdapter().setNextFocusDown(findFocusDown(2));
        host.notifyChanged(binding.episode, host.episodeAdapter());
        host.notifyChanged(binding.part, host.partAdapter());
        host.notifyChanged(binding.flag, host.flagAdapter());
    }

    private boolean shouldEnterFullscreen(Episode item) {
        boolean enter = !host.fullscreen() && item.isSelected();
        if (enter) host.enterFullscreen();
        return enter;
    }

    void checkNext() {
        checkNext(true);
    }

    void checkNext(boolean notify) {
        host.vod().nextEpisode(notify);
    }

    void checkPrev() {
        host.vod().prevEpisode(true);
    }

    void onNext(boolean notify) {
        Episode item = host.episodeAdapter().getNext();
        if (!item.isSelected()) onEpisode(item);
        else if (notify) host.show(host.history().isRevPlay() ? R.string.error_play_prev : R.string.error_play_next);
    }

    void onPrev(boolean notify) {
        Episode item = host.episodeAdapter().getPrev();
        if (!item.isSelected()) onEpisode(item);
        else if (notify) host.show(host.history().isRevPlay() ? R.string.error_play_next : R.string.error_play_prev);
    }
}
