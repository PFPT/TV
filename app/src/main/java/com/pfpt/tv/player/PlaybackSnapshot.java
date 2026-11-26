package com.pfpt.tv.player;

import android.net.Uri;
import android.os.SystemClock;

import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;

import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicReference;

public final class PlaybackSnapshot {

    private static final AtomicReference<Sample> SAMPLE = new AtomicReference<>();

    private PlaybackSnapshot() {
    }

    public static void publish(PlayerManager player) {
        if (player == null || player.isReleased()) {
            SAMPLE.set(null);
            return;
        }
        try {
            MediaItem item = player.getCurrentMediaItem();
            MediaMetadata meta = item != null ? item.mediaMetadata : MediaMetadata.EMPTY;
            long duration = player.getDuration();
            if (duration < 0 || duration == C.TIME_UNSET) duration = 0;
            SAMPLE.set(new Sample(state(player), player.getSpeed(), duration, player.getPosition(), SystemClock.elapsedRealtime(), player.isPlaying(), text(player.getUrl()), text(meta.title), text(meta.artist), text(meta.artworkUri)));
        } catch (RuntimeException ignored) {
            SAMPLE.set(null);
        }
    }

    public static void clear() {
        SAMPLE.set(null);
    }

    public static String json() {
        Sample sample = SAMPLE.get();
        if (sample == null) return "{}";
        long position = sample.position;
        if (sample.playing && sample.speed > 0 && position >= 0) {
            long elapsed = SystemClock.elapsedRealtime() - sample.mark;
            if (elapsed > 0) position += (long) (elapsed * sample.speed);
        }
        if (sample.duration > 0 && position > sample.duration) position = sample.duration;
        JsonObject result = new JsonObject();
        result.addProperty("state", sample.state);
        result.addProperty("speed", sample.speed);
        result.addProperty("duration", sample.duration);
        result.addProperty("position", position);
        result.addProperty("url", sample.url);
        result.addProperty("title", sample.title);
        result.addProperty("artist", sample.artist);
        result.addProperty("artwork", sample.artwork);
        return result.toString();
    }

    private static int state(PlayerManager player) {
        if (player.isPlaying()) return 3;
        int playback = player.getPlaybackState();
        if (playback == Player.STATE_BUFFERING) return 6;
        if (playback == Player.STATE_READY) return 2;
        return 1;
    }

    private static String text(CharSequence value) {
        return value != null ? value.toString() : "";
    }

    private static String text(Uri uri) {
        return uri != null ? uri.toString() : "";
    }

    private static final class Sample {

        private final int state;
        private final float speed;
        private final long duration;
        private final long position;
        private final long mark;
        private final boolean playing;
        private final String url;
        private final String title;
        private final String artist;
        private final String artwork;

        private Sample(int state, float speed, long duration, long position, long mark, boolean playing, String url, String title, String artist, String artwork) {
            this.state = state;
            this.speed = speed;
            this.duration = duration;
            this.position = position;
            this.mark = mark;
            this.playing = playing;
            this.url = url;
            this.title = title;
            this.artist = artist;
            this.artwork = artwork;
        }
    }
}
