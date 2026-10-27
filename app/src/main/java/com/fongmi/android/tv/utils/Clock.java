package com.fongmi.android.tv.utils;

import android.widget.TextView;

import com.fongmi.android.tv.App;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Clock {

    private final List<TextView> views;
    private DateTimeFormatter format;
    private Callback callback;
    private Timer timer;

    public static Clock create() {
        return new Clock();
    }

    public static Clock create(TextView view) {
        return new Clock().view(view).format("HH:mm:ss");
    }

    public Clock() {
        this.views = new ArrayList<>();
    }

    public Clock view(TextView view) {
        views.clear();
        return add(view);
    }

    public Clock add(TextView view) {
        if (view != null) views.add(view);
        return this;
    }

    public Clock format(String format) {
        this.format = DateTimeFormatter.ofPattern(format, Locale.getDefault());
        return this;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void start() {
        stop();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                App.post(() -> doJob());
            }
        }, 0, 1000);
    }

    private void doJob() {
        try {
            long time = System.currentTimeMillis();
            if (callback != null) callback.onTimeChanged(time);
            if (format == null || views.isEmpty()) return;
            String text = format.format(LocalDateTime.now());
            for (TextView view : views) view.setText(text);
        } catch (Exception ignored) {
        }
    }

    public Clock stop() {
        if (timer != null) timer.cancel();
        return this;
    }

    public void release() {
        if (timer != null) timer.cancel();
        if (callback != null) callback = null;
    }

    public interface Callback {

        void onTimeChanged(long time);
    }
}
