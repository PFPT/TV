package com.pfpt.tv.utils;

import com.pfpt.tv.App;
import com.pfpt.tv.bean.Device;
import com.pfpt.tv.server.LanToken;
import com.pfpt.tv.server.Server;
import com.github.catvod.net.OkHttp;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScanTask {

    private final CopyOnWriteArrayList<Future<?>> future;
    private final OkHttpClient client;
    private Listener listener;

    public ScanTask(Listener listener) {
        this.client = OkHttp.client(1000);
        this.future = new CopyOnWriteArrayList<>();
        this.listener = listener;
    }

    public void start() {
        Task.execute(() -> run(getUrl()));
    }

    public void start(String url) {
        Task.execute(() -> run(List.of(url)));
    }

    public void stop() {
        listener = null;
        OkHttp.cancel(client, "scan");
        future.forEach(f -> f.cancel(true));
        future.clear();
    }

    private void run(List<String> urls) {
        for (String url : urls) future.add(Task.submitLarge(() -> findDevice(url)));
    }

    private List<String> getUrl() {
        String local = Server.get().getAddress();
        String base = local.substring(0, local.lastIndexOf(".") + 1);
        return IntStream.range(1, 256).mapToObj(i -> base + i + ":9978").toList();
    }

    private void findDevice(String url) {
        String origin = LanToken.origin(url);
        if (origin.equals(Server.get().getAddress())) return;
        Request request = new Request.Builder().url(origin + "/device").header(LanToken.CLIENT_HEADER, LanToken.CLIENT).tag("scan").build();
        try (Response res = client.newCall(request).execute()) {
            Device device = Device.objectFrom(res.body().string());
            if (device == null) return;
            String token = res.header(LanToken.TOKEN_HEADER);
            if (token == null || token.isEmpty()) token = LanToken.query(url, LanToken.QUERY);
            LanToken.pair(device.getUuid(), token);
            App.post(() -> {
                if (listener != null) listener.onFind(device.save());
            });
        } catch (Exception ignored) {
        }
    }

    public interface Listener {

        void onFind(Device device);
    }
}
