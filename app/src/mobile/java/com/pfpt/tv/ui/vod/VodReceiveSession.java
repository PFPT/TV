package com.pfpt.tv.ui.vod;

import android.app.Activity;

import com.pfpt.tv.api.config.VodConfig;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.event.CastEvent;
import com.pfpt.tv.impl.Callback;
import com.pfpt.tv.ui.activity.VideoActivity;
import com.pfpt.tv.ui.vod.compose.VodReceiveState;
import com.pfpt.tv.utils.Notify;

public final class VodReceiveSession {

    public interface Host {
        void show(VodReceiveState state);

        void update(VodReceiveState state);

        boolean isOpen();

        void dismiss();
    }

    private final Activity activity;
    private final Host host;
    private CastEvent event;

    public VodReceiveSession(Activity activity, Host host) {
        this.activity = activity;
        this.host = host;
    }

    public void show(CastEvent event) {
        if (event == null || event.history() == null) return;
        this.event = event;
        host.show(state(false));
    }

    public void play() {
        if (event == null || event.history() == null) return;
        if (!host.isOpen()) return;
        if (VodConfig.get().getConfig().equals(event.config())) {
            VideoActivity.cast(activity, event.history().save(VodConfig.getCid()));
            dismiss();
            return;
        }
        host.update(state(true));
        VodConfig.load(event.config(), new Callback() {
            @Override
            public void success() {
                play();
            }

            @Override
            public void error(String msg) {
                Notify.show(msg);
                if (host.isOpen()) host.update(state(false));
            }
        });
    }

    public void dismiss() {
        event = null;
        host.dismiss();
    }

    private VodReceiveState state(boolean loading) {
        History item = event.history();
        String from = event.device() == null || event.device().getName() == null ? "" : event.device().getName();
        String name = item.getVodName() == null ? "" : item.getVodName();
        String pic = item.getVodPic() == null ? "" : item.getVodPic();
        return new VodReceiveState(name, from, pic, loading);
    }
}
