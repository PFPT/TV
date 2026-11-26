package com.pfpt.tv.ui.vod;

import com.pfpt.tv.setting.DanmakuSetting;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSettingKeys;
import com.pfpt.tv.ui.vod.compose.VodDanmakuSettingState;

public final class VodDanmakuPrefs {

    private VodDanmakuPrefs() {
    }

    public static boolean writeBool(int key, boolean value) {
        if (key == VodDanmakuSettingKeys.TEXT_BOLD) DanmakuSetting.putTextBold(value);
        else if (key == VodDanmakuSettingKeys.SHOW_SCROLL) DanmakuSetting.putShowScroll(value);
        else if (key == VodDanmakuSettingKeys.SHOW_TOP) DanmakuSetting.putShowTop(value);
        else if (key == VodDanmakuSettingKeys.SHOW_BOTTOM) DanmakuSetting.putShowBottom(value);
        else if (key == VodDanmakuSettingKeys.SHOW_REVERSE) DanmakuSetting.putShowReverse(value);
        else if (key == VodDanmakuSettingKeys.SHOW_POSITIONED) DanmakuSetting.putShowPositioned(value);
        else if (key == VodDanmakuSettingKeys.SHOW_SUBTITLE) DanmakuSetting.putShowSubtitle(value);
        else if (key == VodDanmakuSettingKeys.SHOW_SPECIAL) DanmakuSetting.putShowSpecial(value);
        else return false;
        return true;
    }

    public static boolean writeFloat(int key, float value) {
        if (key == VodDanmakuSettingKeys.TEXT_SCALE) DanmakuSetting.putTextScale(value);
        else if (key == VodDanmakuSettingKeys.TRANSPARENCY) DanmakuSetting.putTransparency(value);
        else if (key == VodDanmakuSettingKeys.SHADOW) DanmakuSetting.putShadowTransparency(value);
        else if (key == VodDanmakuSettingKeys.STROKE) DanmakuSetting.putStrokeWidthMultiplier(value);
        else if (key == VodDanmakuSettingKeys.PROJECTION_X) DanmakuSetting.putProjectionOffsetX(value);
        else if (key == VodDanmakuSettingKeys.PROJECTION_Y) DanmakuSetting.putProjectionOffsetY(value);
        else if (key == VodDanmakuSettingKeys.PROJECTION_ALPHA) DanmakuSetting.putProjectionTransparency(value);
        else if (key == VodDanmakuSettingKeys.TIME_OFFSET) DanmakuSetting.putTimeOffsetMs((long) value);
        else if (key == VodDanmakuSettingKeys.DURATION) DanmakuSetting.putDurationMs((long) value);
        else if (key == VodDanmakuSettingKeys.FIXED_DURATION) DanmakuSetting.putFixedDurationMs((long) value);
        else if (key == VodDanmakuSettingKeys.MAX_ON_SCREEN) DanmakuSetting.putMaxOnScreen(Math.round(value));
        else if (key == VodDanmakuSettingKeys.SCROLL_AREA) DanmakuSetting.putScrollAreaRatio(value);
        else if (key == VodDanmakuSettingKeys.SCROLL_GAP) DanmakuSetting.putScrollGapRatio(value);
        else if (key == VodDanmakuSettingKeys.LINE_SPACING) DanmakuSetting.putLineSpacing(value);
        else if (key == VodDanmakuSettingKeys.MAX_SCROLL) DanmakuSetting.putMaxScrollLines(Math.round(value));
        else if (key == VodDanmakuSettingKeys.MAX_TOP) DanmakuSetting.putMaxTopLines(Math.round(value));
        else if (key == VodDanmakuSettingKeys.MAX_BOTTOM) DanmakuSetting.putMaxBottomLines(Math.round(value));
        else return false;
        return true;
    }

    public static boolean writeMode(int key, int value) {
        if (key == VodDanmakuSettingKeys.STYLE) DanmakuSetting.putStyleMode(value);
        else if (key == VodDanmakuSettingKeys.COLOR) DanmakuSetting.putColorMode(value);
        else return false;
        return true;
    }

    public static VodDanmakuSettingState read(int tab) {
        return new VodDanmakuSettingState(
                tab,
                DanmakuSetting.isTextBold(),
                DanmakuSetting.getTextScale(),
                DanmakuSetting.getTransparency(),
                DanmakuSetting.getShadowTransparency(),
                DanmakuSetting.getStrokeWidthMultiplier(),
                DanmakuSetting.getProjectionOffsetX(),
                DanmakuSetting.getProjectionOffsetY(),
                DanmakuSetting.getProjectionTransparency(),
                DanmakuSetting.getStyleMode(),
                DanmakuSetting.getColorMode(),
                (float) DanmakuSetting.getTimeOffsetMs(),
                (float) DanmakuSetting.getDurationMs(),
                (float) DanmakuSetting.getFixedDurationMs(),
                DanmakuSetting.getMaxOnScreen(),
                DanmakuSetting.getScrollAreaRatio(),
                DanmakuSetting.getScrollGapRatio(),
                DanmakuSetting.getLineSpacing(),
                DanmakuSetting.getMaxScrollLines(),
                DanmakuSetting.getMaxTopLines(),
                DanmakuSetting.getMaxBottomLines(),
                DanmakuSetting.isShowScroll(),
                DanmakuSetting.isShowTop(),
                DanmakuSetting.isShowBottom(),
                DanmakuSetting.isShowReverse(),
                DanmakuSetting.isShowPositioned(),
                DanmakuSetting.isShowSubtitle(),
                DanmakuSetting.isShowSpecial()
        );
    }
}
