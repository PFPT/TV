package com.pfpt.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.media3.common.C;
import androidx.viewbinding.ViewBinding;

import com.pfpt.tv.App;
import com.pfpt.tv.R;
import com.pfpt.tv.bean.Parse;
import com.pfpt.tv.databinding.DialogControlBinding;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.ui.adapter.ParseAdapter;
import com.pfpt.tv.ui.custom.SpaceItemDecoration;
import com.pfpt.tv.utils.ResUtil;
import com.pfpt.tv.utils.Timer;
import com.google.android.material.slider.Slider;

import java.util.Arrays;
import java.util.List;

public class ControlDialog extends BaseBottomSheetDialog implements ParseAdapter.OnClickListener {

    private final String[] scale;
    private DialogControlBinding binding;
    private List<TextView> scales;
    private PlayerManager player;
    private boolean parse;

    public ControlDialog() {
        this.scale = ResUtil.getStringArray(R.array.select_scale);
    }

    public static ControlDialog create() {
        return new ControlDialog();
    }

    public ControlDialog parse(boolean parse) {
        this.parse = parse;
        return this;
    }

    public ControlDialog player(PlayerManager player) {
        this.player = player;
        return this;
    }

    public ControlDialog show(FragmentActivity activity) {
        for (Fragment f : activity.getSupportFragmentManager().getFragments()) if (f instanceof ControlDialog) return this;
        show(activity.getSupportFragmentManager(), null);
        return this;
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        binding = DialogControlBinding.inflate(inflater, container, false);
        scales = Arrays.asList(binding.scale0, binding.scale1, binding.scale2, binding.scale3, binding.scale4);
        return binding;
    }

    @Override
    protected void initView() {
        binding.decode.setText(player.getDecodeText());
        binding.ending.setText(listener().endingLabel());
        binding.opening.setText(listener().openingLabel());
        binding.repeat.setSelected(player.isRepeatOne());
        binding.timer.setSelected(Timer.get().isRunning());
        setMediaOptionVisible();
        setTrackVisible();
        setScaleText();
        setPlayer();
        setParse();
    }

    @Override
    protected void initEvent() {
        binding.timer.setOnClickListener(this::onTimer);
        binding.speed.addOnChangeListener(this::setSpeed);
        for (TextView view : scales) view.setOnClickListener(this::setScale);
        binding.text.setOnClickListener(v -> runDismiss(() -> listener().onTrack(C.TRACK_TYPE_TEXT)));
        binding.audio.setOnClickListener(v -> runDismiss(() -> listener().onTrack(C.TRACK_TYPE_AUDIO)));
        binding.video.setOnClickListener(v -> runDismiss(() -> listener().onTrack(C.TRACK_TYPE_VIDEO)));
        binding.player.setOnClickListener(v -> runDismiss(() -> listener().onChoose()));
        binding.danmaku.setOnClickListener(v -> runDismiss(() -> listener().onDanmaku()));
        binding.edition.setOnClickListener(v -> runDismiss(() -> listener().onEdition()));
        binding.chapter.setOnClickListener(v -> runDismiss(() -> listener().onChapter()));
        binding.repeat.setOnClickListener(v -> onRepeat());
        binding.decode.setOnClickListener(v -> onDecode());
        binding.ending.setOnClickListener(v -> onEnding());
        binding.opening.setOnClickListener(v -> onOpening());
        binding.ending.setOnLongClickListener(v -> onEndingReset());
        binding.opening.setOnLongClickListener(v -> onOpeningReset());
    }

    private void onTimer(View view) {
        TimerDialog.create().show(getActivity());
        dismiss();
    }

    private void setSpeed(@NonNull Slider slider, float value, boolean fromUser) {
        listener().onSpeedChanged(value);
    }

    private void setScaleText() {
        int current = listener().getScale();
        for (int i = 0; i < scales.size(); i++) {
            scales.get(i).setText(scale[i]);
            scales.get(i).setSelected(i == current);
        }
    }

    private void setParse() {
        setParseVisible(parse);
        binding.parse.setHasFixedSize(true);
        binding.parse.setItemAnimator(null);
        binding.parse.addItemDecoration(new SpaceItemDecoration(8));
        binding.parse.setAdapter(new ParseAdapter(this));
    }

    private void setScale(View view) {
        for (TextView textView : scales) textView.setSelected(false);
        listener().onScale(Integer.parseInt(view.getTag().toString()));
        view.setSelected(true);
    }

    private void onRepeat() {
        listener().onRepeat();
        binding.repeat.setSelected(player.isRepeatOne());
    }

    private void onDecode() {
        listener().onDecode();
        binding.decode.setText(player.getDecodeText());
    }

    private void onEnding() {
        listener().onEnding();
        binding.ending.setText(listener().endingLabel());
    }

    private void onOpening() {
        listener().onOpening();
        binding.opening.setText(listener().openingLabel());
    }

    private boolean onEndingReset() {
        listener().onEndingReset();
        binding.ending.setText(listener().endingLabel());
        return true;
    }

    private boolean onOpeningReset() {
        listener().onOpeningReset();
        binding.opening.setText(listener().openingLabel());
        return true;
    }

    private void runDismiss(Runnable action) {
        App.post(action, 200);
        dismiss();
    }

    public void setPlayer() {
        binding.speed.setValue(Math.max(player.getSpeed(), 0.5f));
        binding.player.setText(PlaybackAction.getEngineText(player));
        binding.decode.setVisibility(View.VISIBLE);
        binding.danmaku.setVisibility(DanmakuSetting.isLoad() ? View.VISIBLE : View.GONE);
    }

    public void setParseVisible(boolean visible) {
        binding.parse.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.parseText.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setTrackVisible() {
        PlaybackAction.setTracks(player, binding.text, binding.audio, binding.video);
        binding.track.setVisibility(binding.text.getVisibility() == View.GONE && binding.audio.getVisibility() == View.GONE && binding.video.getVisibility() == View.GONE ? View.GONE : View.VISIBLE);
    }

    public void setMediaOptionVisible() {
        PlaybackAction.setMediaOptions(player, binding.edition, binding.chapter);
    }

    private Listener listener() {
        return (Listener) requireActivity();
    }

    @Override
    public void onItemClick(Parse item) {
        listener().onParse(item);
        binding.parse.getAdapter().notifyItemRangeChanged(0, binding.parse.getAdapter().getItemCount());
    }

    public interface Listener {

        void onScale(int tag);

        void onParse(Parse item);

        int getScale();

        void onTrack(int type);

        void onEdition();

        void onChapter();

        void onDanmaku();

        void onChoose();

        void onRepeat();

        void onDecode();

        void onEnding();

        void onOpening();

        boolean onEndingReset();

        boolean onOpeningReset();

        void onSpeedChanged(float speed);

        String openingLabel();

        String endingLabel();
    }
}
