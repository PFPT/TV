package com.pfpt.tv.playback.vod;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.pfpt.tv.api.SiteApi;
import com.pfpt.tv.bean.History;
import com.pfpt.tv.utils.FileChooser;
import com.pfpt.tv.utils.Sniffer;
import com.pfpt.tv.utils.UrlUtil;

import java.io.File;
import java.util.Objects;

public final class VodLaunch {

    private VodLaunch() {
    }

    public static void push(FragmentActivity activity, Class<?> target, String text) {
        Uri uri = UrlUtil.uri(text);
        if (FileChooser.isValid(activity, uri)) file(activity, target, FileChooser.getPathFromUri(uri));
        else start(activity, target, Sniffer.getUrl(text));
    }

    public static void file(FragmentActivity activity, Class<?> target, String path) {
        if (path == null || path.isEmpty()) return;
        String name = new File(path).getName();
        start(activity, target, SiteApi.PUSH, "file://" + path, name);
    }

    public static void cast(Activity activity, Class<?> target, History history) {
        start(activity, target, history.getSiteKey(), history.getVodId(), history.getVodName(), history.getVodPic(), null, false, true);
    }

    public static void collect(Activity activity, Class<?> target, String key, String id, String name, String pic) {
        start(activity, target, key, id, name, pic, null, true, false);
    }

    public static void start(Activity activity, Class<?> target, String url) {
        start(activity, target, SiteApi.PUSH, url, url);
    }

    public static void start(Activity activity, Class<?> target, String key, String id, String name) {
        start(activity, target, key, id, name, null);
    }

    public static void start(Activity activity, Class<?> target, String key, String id, String name, String pic) {
        start(activity, target, key, id, name, pic, null);
    }

    public static void start(Activity activity, Class<?> target, String key, String id, String name, String pic, String mark) {
        start(activity, target, key, id, name, pic, mark, false, false);
    }

    public static void start(Activity activity, Class<?> target, String key, String id, String name, String pic, String mark, boolean collect, boolean cast) {
        Intent intent = new Intent(activity, target);
        intent.putExtra("collect", collect);
        intent.putExtra("cast", cast);
        intent.putExtra("mark", mark);
        intent.putExtra("name", name);
        intent.putExtra("pic", pic);
        intent.putExtra("key", key);
        intent.putExtra("id", id);
        activity.startActivity(intent);
    }

    public static String text(Intent intent, String key) {
        return Objects.toString(intent.getStringExtra(key), "");
    }

    public static boolean flag(Intent intent, String key) {
        return intent.getBooleanExtra(key, false);
    }
}
