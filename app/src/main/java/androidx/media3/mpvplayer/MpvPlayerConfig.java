package androidx.media3.mpvplayer;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.media3.common.util.UnstableApi;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@UnstableApi
public final class MpvPlayerConfig {

  public static final String VIDEO_OUTPUT_GPU_NEXT = "gpu-next";

  private MpvPlayerConfig() {}

  public static final class Builder {
    private final List<Object> unused = new ArrayList<>();

    public Builder setDefaultUserAgent(@Nullable String userAgent) {
      return this;
    }

    public Builder setHlsHttpPersistent(boolean persistent) {
      return this;
    }

    public Builder addConfigDirectory(File directory) {
      return this;
    }

    public Builder addAndroidFontConfig(File configDir, File cacheDir) {
      return this;
    }

    public Builder addAndroidDefaults(@Nullable String videoOutput, File cacheDir) {
      return this;
    }

    public Builder addTlsCaFileFromAsset(Context context, String assetName, File dest) {
      return this;
    }

    public Builder addPostInitStringOption(String key, @Nullable String value) {
      return this;
    }

    public Builder addPostInitStringOption(String key, List<String> values) {
      return this;
    }

    public Builder addPreInitStringOption(String key, String value) {
      return this;
    }

    public Builder addDiskCacheOptions(File cacheDir, int preloadTimeSeconds, int preloadSizeMb) {
      return this;
    }

    public Builder addAndroidSubtitleOptions(
        Context context, boolean caption, double position, double scale) {
      return this;
    }

    public MpvPlayerConfig build() {
      return new MpvPlayerConfig();
    }
  }
}
