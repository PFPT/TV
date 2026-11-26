package com.pfpt.tv.ui.activity;

import android.widget.TextView;

import com.pfpt.tv.R;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.player.PlayerManager;
import com.pfpt.tv.playback.PlaybackAction;
import com.pfpt.tv.playback.vod.VodPlaybackController;
import com.pfpt.tv.utils.Util;

public final class LeanbackSpanControls {

    public void speed(VodPlaybackController vod, PlayerManager player, TextView speed) {
        vod.setSpeed(PlaybackAction.addSpeed(player, speed));
    }

    public void speedAdd(VodPlaybackController vod, PlayerManager player, TextView speed) {
        vod.setSpeed(PlaybackAction.addSpeed(player, speed, 0.25f));
    }

    public void speedSub(VodPlaybackController vod, PlayerManager player, TextView speed) {
        vod.setSpeed(PlaybackAction.subSpeed(player, speed, 0.25f));
    }

    public void opening(VodPlaybackController vod, PlayerManager player, History history, TextView view) {
        long position = player.getPosition();
        long duration = player.getDuration();
        if (player.canSetOpening(position, duration)) setOpening(vod, history, view, position);
    }

    public void openingAdd(VodPlaybackController vod, History history, TextView view) {
        setOpening(vod, history, view, Math.max(0, Math.max(0, history.getOpening()) + 1000));
    }

    public void openingSub(VodPlaybackController vod, History history, TextView view) {
        setOpening(vod, history, view, Math.max(0, Math.max(0, history.getOpening()) - 1000));
    }

    public void openingReset(VodPlaybackController vod, History history, TextView view) {
        setOpening(vod, history, view, 0);
    }

    public void ending(VodPlaybackController vod, PlayerManager player, History history, TextView view) {
        long position = player.getPosition();
        long duration = player.getDuration();
        if (player.canSetEnding(position, duration)) setEnding(vod, history, view, duration - position);
    }

    public void endingAdd(VodPlaybackController vod, History history, TextView view) {
        setEnding(vod, history, view, Math.max(0, Math.max(0, history.getEnding()) + 1000));
    }

    public void endingSub(VodPlaybackController vod, History history, TextView view) {
        setEnding(vod, history, view, Math.max(0, Math.max(0, history.getEnding()) - 1000));
    }

    public void endingReset(VodPlaybackController vod, History history, TextView view) {
        setEnding(vod, history, view, 0);
    }

    private void setOpening(VodPlaybackController vod, History history, TextView view, long opening) {
        vod.setOpening(opening);
        view.setText(opening <= 0 ? view.getContext().getString(R.string.play_op) : Util.timeMs(history.getOpening()));
    }

    private void setEnding(VodPlaybackController vod, History history, TextView view, long ending) {
        vod.setEnding(ending);
        view.setText(ending <= 0 ? view.getContext().getString(R.string.play_ed) : Util.timeMs(history.getEnding()));
    }
}
