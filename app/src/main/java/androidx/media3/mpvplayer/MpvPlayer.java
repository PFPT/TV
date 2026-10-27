package androidx.media3.mpvplayer;

import android.content.Context;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.SimpleBasePlayer;
import androidx.media3.common.util.UnstableApi;

@UnstableApi
public final class MpvPlayer extends SimpleBasePlayer {

  public static boolean isAvailable() {
    return false;
  }

  private MpvPlayer(Looper looper) {
    super(looper);
  }

  @Override
  protected State getState() {
    return new State.Builder().build();
  }

  public void setSubtitleOptions(MpvPlayerConfig config) {}

  public void addSubtitle(MediaItem.SubtitleConfiguration configuration) {}

  public void setDecode(int decode) {}

  public static final class Builder {
    public Builder(Context context) {}

    public Builder setDecode(int decode) {
      return this;
    }

    public Builder setConfig(@Nullable MpvPlayerConfig config) {
      return this;
    }

    public MpvPlayer build() {
      return new MpvPlayer(Looper.getMainLooper());
    }
  }
}
