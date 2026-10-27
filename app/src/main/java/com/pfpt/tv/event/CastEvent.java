package com.pfpt.tv.event;

import com.pfpt.tv.bean.Config;
import com.pfpt.tv.bean.Device;
import com.pfpt.tv.bean.History;

import org.greenrobot.eventbus.EventBus;

public record CastEvent(Config config, Device device, History history) {

    public static void post(Config config, Device device, History history) {
        EventBus.getDefault().post(new CastEvent(config, device, history));
    }
}
