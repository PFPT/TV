package com.pfpt.tv.ui.vod;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pfpt.tv.App;
import com.pfpt.tv.Constant;
import com.pfpt.tv.R;
import com.pfpt.tv.bean.CastVideo;
import com.pfpt.tv.bean.Config;
import com.pfpt.tv.bean.Device;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.dlna.DLNACast;
import com.pfpt.tv.dlna.DLNACastManager;
import com.pfpt.tv.server.Server;
import com.pfpt.tv.ui.vod.compose.VodCastEvent;
import com.pfpt.tv.ui.vod.compose.VodCastIds;
import com.pfpt.tv.ui.vod.compose.VodCastState;
import com.pfpt.tv.ui.vod.compose.VodSheetController;
import com.pfpt.tv.ui.vod.compose.VodSheetKind;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.ScanTask;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public final class VodCastSession {

    private final Context context;
    private final VodSheetController sheets;
    private final Runnable onCasted;
    private final Runnable onScan;
    private final List<Device> devices = new ArrayList<>();
    private FormBody.Builder body;
    private OkHttpClient client;
    private CastVideo video;
    private ScanTask scanTask;
    private final DLNACastManager.DeviceListener deviceListener = new DLNACastManager.DeviceListener() {
        @Override
        public void onDeviceAdded(Device device) {
            add(device);
        }

        @Override
        public void onDeviceRemoved(Device device) {
            remove(device);
        }
    };
    private final okhttp3.Callback httpCallback = new okhttp3.Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            App.post(() -> Notify.show(e.getMessage()));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            try (Response res = response) {
                if (res.body() != null && res.body().string().equals("OK")) App.post(VodCastSession.this::finish);
                else App.post(() -> Notify.show(R.string.device_offline));
            }
        }
    };

    public VodCastSession(Context context, VodSheetController sheets, Runnable onCasted, Runnable onScan) {
        this.context = context;
        this.sheets = sheets;
        this.onCasted = onCasted;
        this.onScan = onScan;
    }

    public void open(String name, String url, long position, Map<String, String> headers, History history) {
        release();
        video = new CastVideo(name, url, position, headers);
        client = OkHttp.client(Constant.TIMEOUT_SYNC);
        body = new FormBody.Builder();
        body.add("device", Device.get().toString());
        body.add("config", Config.vod().toString());
        String id = history.getVodId();
        String fd = id;
        if (fd != null && fd.startsWith("/")) fd = Server.get().getAddress() + "/file" + fd.replace(Path.rootPath(), "");
        if (fd != null && fd.startsWith("file")) fd = Server.get().getAddress() + "/" + fd.replace(Path.rootPath(), "").replace("://", "");
        if (fd != null && fd.contains("127.0.0.1")) fd = fd.replace("127.0.0.1", com.github.catvod.utils.Util.getIp());
        String saved = history.toString();
        if (id != null && fd != null) saved = saved.replace(id, fd);
        body.add("history", saved);
        scanTask = new ScanTask(this::add);
        DLNACastManager.get().init(context);
        DLNACastManager.get().setDeviceListener(deviceListener);
        sheets.showCast(new VodCastState(context.getString(R.string.device_select), List.of(), List.of()));
        load();
    }

    public void scan(String address) {
        if (scanTask != null) scanTask.start(address);
    }

    public void onEvent(VodCastEvent event) {
        int id = event.getId();
        if (id == VodCastIds.SCAN) {
            onScan.run();
            return;
        }
        if (id == VodCastIds.REFRESH) {
            refresh();
            return;
        }
        if (id != VodCastIds.DEVICE || body == null || client == null || video == null) return;
        int index = event.getIndex();
        if (index < 0 || index >= devices.size()) return;
        Device item = devices.get(index);
        if (item.isDLNA()) new DLNACast(video, this::finish).cast(item);
        else OkHttp.newCall(client, item.getIp().concat("/action?do=cast"), body.build()).enqueue(httpCallback);
    }

    public void dismiss() {
        release();
        if (sheets.getKind() == VodSheetKind.Cast) sheets.dismiss();
    }

    public void release() {
        DLNACastManager.get().setDeviceListener(null);
        DLNACastManager.get().release(context);
        if (scanTask != null) {
            scanTask.stop();
            scanTask = null;
        }
    }

    private void load() {
        List<Device> saved = Device.getAll();
        set(saved == null ? List.of() : saved);
        List<Device> registered = DLNACastManager.get().getRegistered();
        if (registered != null && !registered.isEmpty()) addAll(registered);
        if (devices.isEmpty()) refresh();
        else DLNACastManager.get().search();
    }

    private void refresh() {
        devices.clear();
        publish();
        Device.delete();
        if (scanTask != null) scanTask.start();
        DLNACastManager.get().search();
        List<Device> registered = DLNACastManager.get().getRegistered();
        if (registered != null && !registered.isEmpty()) addAll(registered);
    }

    private void set(List<Device> items) {
        devices.clear();
        if (items != null) devices.addAll(items);
        publish();
    }

    private void add(Device device) {
        if (device == null || sheets.getKind() != VodSheetKind.Cast) return;
        set(Stream.concat(devices.stream(), Stream.of(device)).distinct().sorted().toList());
    }

    private void addAll(List<Device> items) {
        if (items == null || items.isEmpty() || sheets.getKind() != VodSheetKind.Cast) return;
        set(Stream.concat(devices.stream(), items.stream()).distinct().sorted().toList());
    }

    private void remove(Device device) {
        if (sheets.getKind() != VodSheetKind.Cast) return;
        if (devices.remove(device)) publish();
    }

    private void publish() {
        List<String> names = new ArrayList<>();
        List<String> hosts = new ArrayList<>();
        for (Device device : devices) {
            names.add(device.getName() == null ? "" : device.getName());
            hosts.add(device.getHost() == null ? "" : device.getHost());
        }
        sheets.updateCast(new VodCastState(context.getString(R.string.device_select), names, hosts));
    }

    private void finish() {
        if (sheets.getKind() != VodSheetKind.Cast) return;
        onCasted.run();
        dismiss();
    }
}
