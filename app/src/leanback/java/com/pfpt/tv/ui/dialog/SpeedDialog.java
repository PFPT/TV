package com.pfpt.tv.ui.dialog;

import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.pfpt.tv.databinding.DialogSpeedBinding;
import com.pfpt.tv.impl.SpeedListener;
import com.pfpt.tv.setting.PlayerSetting;
import com.pfpt.tv.utils.KeyUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SpeedDialog extends BaseAlertDialog {

    private DialogSpeedBinding binding;

    public static void show(FragmentActivity activity) {
        new SpeedDialog().show(activity.getSupportFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogSpeedBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder().setView(getBinding().getRoot());
    }

    @Override
    protected void initView() {
        binding.slider.setValue(PlayerSetting.getSpeed());
    }

    @Override
    protected void initEvent() {
        binding.slider.addOnChangeListener((slider, value, fromUser) -> ((SpeedListener) requireActivity()).setSpeed(value));
        binding.slider.setOnKeyListener((view, keyCode, event) -> {
            boolean enter = KeyUtil.isEnterKey(event);
            if (enter) dismiss();
            return enter;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}