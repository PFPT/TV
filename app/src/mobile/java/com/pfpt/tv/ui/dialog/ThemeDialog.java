package com.pfpt.tv.ui.dialog;

import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.pfpt.tv.R;
import com.pfpt.tv.databinding.DialogThemeBinding;
import com.pfpt.tv.setting.Setting;
import com.pfpt.tv.ui.custom.ColorPickerView;
import com.pfpt.tv.ui.custom.CustomTextListener;
import com.pfpt.tv.ui.custom.ThemePalette;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class ThemeDialog extends BaseAlertDialog implements ColorPickerView.Listener {

    private static final int DEFAULT_COLOR = 0xFF6750A4;
    private DialogThemeBinding binding;
    private boolean writingHex;
    private int mode;

    public static void show(Fragment fragment) {
        new ThemeDialog().show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogThemeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setTitle(R.string.setting_theme_color).setView(getBinding().getRoot()).setNegativeButton(R.string.dialog_negative, null).setPositiveButton(R.string.dialog_positive, (dialog, which) -> apply());
    }

    @Override
    protected void initView() {
        int stored = Setting.getThemeColor();
        int seed = stored == -1 || stored == 0 ? seedFallback() : stored;
        mode = stored == -1 ? -1 : stored == 0 ? 0 : 1;
        binding.picker.setColor(seed);
        showHex(seed);
        showPalette(seed);
        showCustom(mode > 0);
    }

    @Override
    protected void initEvent() {
        binding.picker.setListener(this);
        binding.modeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.modeOff) mode = -1;
            else if (checkedId == R.id.modeAuto) mode = 0;
            else mode = 1;
            showCustom(mode > 0);
        });
        binding.hex.addTextChangedListener(new CustomTextListener() {
            @Override
            public void afterTextChanged(Editable s) {
                if (writingHex) return;
                int color = parseHex(s.toString());
                if (color == 0) return;
                binding.picker.setColor(color);
                showPalette(color);
            }
        });
        int stored = Setting.getThemeColor();
        binding.modeGroup.check(stored == -1 ? R.id.modeOff : stored == 0 ? R.id.modeAuto : R.id.modeCustom);
    }

    @Override
    public void onColorChanged(int color) {
        showHex(color);
        showPalette(color);
    }

    private void apply() {
        int color = mode < 0 ? -1 : mode == 0 ? 0 : binding.picker.getColor();
        ((Listener) requireParentFragment()).setTheme(color);
    }

    private void showCustom(boolean custom) {
        int visibility = custom ? View.VISIBLE : View.GONE;
        binding.picker.setVisibility(visibility);
        binding.hexInput.setVisibility(visibility);
        binding.palette.setVisibility(visibility);
    }

    private void showHex(int color) {
        writingHex = true;
        String text = String.format(Locale.US, "#%06X", color & 0xFFFFFF);
        binding.hex.setText(text);
        binding.hex.setSelection(text.length());
        writingHex = false;
    }

    private void showPalette(int color) {
        ThemePalette palette = ThemePalette.of(color, getResources().getConfiguration().uiMode);
        paintSwatch(binding.swatchPrimary, palette.primary);
        paintSwatch(binding.swatchSecondary, palette.secondary);
        paintSwatch(binding.swatchTertiary, palette.tertiary);
        paintSwatch(binding.swatchSurface, palette.surface);
        paintSwatch(binding.swatchBackground, palette.background);
    }

    private void paintSwatch(View view, int color) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        view.setBackground(circle);
    }

    private int seedFallback() {
        int wall = Setting.getWallColor();
        return wall != 0 ? wall : DEFAULT_COLOR;
    }

    private int parseHex(String text) {
        String value = text.trim();
        if (value.startsWith("#")) value = value.substring(1);
        if (value.length() != 6) return 0;
        try {
            return 0xFF000000 | Integer.parseInt(value, 16);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public interface Listener {

        void setTheme(int color);
    }
}
