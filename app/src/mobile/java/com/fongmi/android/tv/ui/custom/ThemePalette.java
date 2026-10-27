package com.fongmi.android.tv.ui.custom;

import android.content.res.Configuration;

import com.google.android.material.color.utilities.Hct;
import com.google.android.material.color.utilities.SchemeContent;

public class ThemePalette {

    public final int primary;
    public final int secondary;
    public final int tertiary;
    public final int surface;
    public final int background;

    private ThemePalette(int primary, int secondary, int tertiary, int surface, int background) {
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.surface = surface;
        this.background = background;
    }

    public static ThemePalette of(int seed, int uiMode) {
        boolean dark = (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        SchemeContent scheme = new SchemeContent(Hct.fromInt(seed | 0xFF000000), dark, 0);
        return new ThemePalette(scheme.getPrimary(), scheme.getSecondary(), scheme.getTertiary(), scheme.getSurface(), scheme.getBackground());
    }
}
