package com.fongmi.android.tv.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogDownloadBinding;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.Util;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class DownloadDialog extends BaseAlertDialog {

    private static final String PLAYER = "com.zhaoshuo.localvideoplayer";
    private static final String[] EXTS = {"m3u8", "mp4", "mkv", "flv", "ts", "avi", "webm", "mov", "rmvb", "wmv", "m4v"};

    private DialogDownloadBinding binding;
    private String name;
    private String url;
    private long durationMs;

    public static void show(FragmentActivity activity, String name, String url, long durationMs) {
        if (TextUtils.isEmpty(url)) return;
        DownloadDialog dialog = new DownloadDialog();
        dialog.name = name == null ? "" : name;
        dialog.url = url;
        dialog.durationMs = durationMs;
        dialog.show(activity.getSupportFragmentManager(), null);
    }

    public static void openList(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(PLAYER);
        if (intent == null) {
            Notify.show(R.string.download_missing);
            return;
        }
        activity.startActivity(intent);
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
        try {
            requireContext().getPackageManager().getPackageInfo(PLAYER, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void send() {
        if (TextUtils.isEmpty(url) || !isPlayerInstalled()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(PLAYER);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.putExtra(Intent.EXTRA_SUBJECT, name);
        startActivity(intent);
        dismiss();
    }

    static String extension(String url) {
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
