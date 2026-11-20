package com.pfpt.tv.ui.dialog;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.pfpt.tv.R;
import com.pfpt.tv.databinding.DialogDownloadBinding;
import com.pfpt.tv.player.util.PlayerHelper;
import com.pfpt.tv.utils.Notify;
import com.pfpt.tv.utils.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Map;

public class DownloadDialog extends BaseAlertDialog {

    private static final String PLAYER = "com.pfpt.localvideoplayer";
    private static final String[] EXTS = {"m3u8", "mp4", "mkv", "flv", "ts", "avi", "webm", "mov", "rmvb", "wmv", "m4v"};

    private DialogDownloadBinding binding;
    private String name;
    private String url;
    private long durationMs;
    private Map<String, String> headers;

    public static void show(FragmentActivity activity, String name, String url, long durationMs, Map<String, String> headers) {
        if (TextUtils.isEmpty(url)) return;
        DownloadDialog dialog = new DownloadDialog();
        dialog.name = name == null ? "" : name;
        dialog.url = url;
        dialog.durationMs = durationMs;
        dialog.headers = headers;
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    public static String fileName(String title, String episode) {
        String name = title == null ? "" : title;
        String part = episode == null ? "" : episode;
        if (TextUtils.isEmpty(name)) return part;
        if (TextUtils.isEmpty(part)) return name;
        return name + "-" + part;
    }

    public static void openList(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(PLAYER);
        if (intent == null) {
            Notify.show(R.string.download_missing);
            return;
        }
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Notify.show(R.string.download_missing);
        }
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogDownloadBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        if (name == null) name = "";
        if (url == null) url = "";
        boolean installed = isPlayerInstalled();
        binding.name.setText(name);
        binding.url.setText(url);
        binding.ext.setText(extension(url));
        binding.duration.setVisibility(durationMs > 0 ? View.VISIBLE : View.GONE);
        if (durationMs > 0) binding.duration.setText(getString(R.string.download_duration, Util.timeMs(durationMs)));
        binding.status.setVisibility(installed ? View.GONE : View.VISIBLE);
        binding.send.setEnabled(installed);
    }

    @Override
    protected void initEvent() {
        binding.copy.setOnClickListener(view -> Util.copy(url));
        binding.cancel.setOnClickListener(view -> dismiss());
        binding.send.setOnClickListener(view -> send());
    }

    private boolean isPlayerInstalled() {
        return isPlayerInstalled(requireContext());
    }

    public static boolean isPlayerInstalled(Context context) {
        if (context == null) return false;
        try {
            context.getPackageManager().getPackageInfo(PLAYER, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void send() {
        if (send(requireActivity(), name, url, headers)) dismiss();
    }

    public static boolean send(Activity activity, String name, String url, Map<String, String> headers) {
        if (activity == null || TextUtils.isEmpty(url) || !isPlayerInstalled(activity)) {
            Notify.show(R.string.download_missing);
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLAYER);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.putExtra(Intent.EXTRA_SUBJECT, name == null ? "" : name);
        intent.putExtra("extra_headers", PlayerHelper.toBundle(headers));
        try {
            activity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            Notify.show(R.string.download_missing);
            return false;
        }
    }

    public static String extension(String url) {
        if (url != null && url.toLowerCase(Locale.US).contains("do=m3u8")) return "m3u8";
        String media = unwrapLocalProxy(url);
        String path = Uri.parse(media).getPath();
        if (path == null) path = media;
        int slash = path.lastIndexOf('/');
        String file = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = file.lastIndexOf('.');
        if (dot < 0 || dot == file.length() - 1) return "mp4";
        String ext = file.substring(dot + 1).toLowerCase(Locale.US);
        for (String known : EXTS) if (known.equals(ext)) return ext;
        return "mp4";
    }

    static String unwrapLocalProxy(String url) {
        String current = url == null ? "" : url;
        for (int i = 0; i < 4; i++) {
            Uri uri = Uri.parse(current);
            String host = uri.getHost();
            if (host == null) return current;
            String lower = host.toLowerCase(Locale.US);
            if (!lower.equals("127.0.0.1") && !lower.equals("localhost") && !lower.equals("[::1]")) return current;
            String inner = uri.getQueryParameter("url");
            if (TextUtils.isEmpty(inner) || inner.equals(current)) return current;
            current = inner;
        }
        return current;
    }
}
