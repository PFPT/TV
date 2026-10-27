package androidx.media3.exoplayer.source.preload;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PriorityTaskManager;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.source.MediaSource;

@UnstableApi
public final class DiskPreloadManager {

    private DiskPreloadManager() {}

    public void start(ExoPlayer player, MediaItem mediaItem, Options options) {}

    public void release() {}

    public static final class Options {
        private final long durationMs;
        private final int maxThreads;

        private Options(long durationMs, int maxThreads) {
            this.durationMs = durationMs;
            this.maxThreads = maxThreads;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private long durationMs;
            private int maxThreads = 1;

            public Builder setDurationMs(long durationMs) {
                this.durationMs = durationMs;
                return this;
            }

            public Builder setMaxThreads(int maxThreads) {
                this.maxThreads = maxThreads;
                return this;
            }

            public Options build() {
                return new Options(durationMs, maxThreads);
            }
        }
    }

    public static final class Builder {
        public Builder(Cache cache, DataSource.Factory upstreamFactory, RenderersFactory renderersFactory) {}

        public Builder setPriorityTaskManager(PriorityTaskManager priorityTaskManager) {
            return this;
        }

        public Builder setMediaSourceFactory(MediaSource.Factory mediaSourceFactory) {
            return this;
        }

        public DiskPreloadManager build() {
            return new DiskPreloadManager();
        }
    }
}
