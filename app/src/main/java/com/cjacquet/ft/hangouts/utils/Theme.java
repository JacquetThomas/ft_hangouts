package com.cjacquet.ft.hangouts.utils;

import com.cjacquet.ft.hangouts.R;

public enum Theme {
    DEFAULT(R.style.AppTheme, R.color.colorPrimary, "orange"),
    ORANGE(R.style.OrangeTheme, R.color.orangeColorPrimary, "orange"),
    BLUE(R.style.BlueTheme, R.color.blueColorPrimary, "blue"),
    GREEN(R.style.GreenTheme, R.color.greenColorPrimary, "green");

    private final int themeId;
    private final int primaryColorId;
    private final String colorString;

    Theme(final int value, final int primaryColorId, final String colorString) {
        this.themeId = value;
        this.primaryColorId = primaryColorId;
        this.colorString = colorString;
    }

    public static Theme valueOf(int themeId) {
        for (Theme t : values()) {
            if (t.getThemeId() == themeId)
                return t;
        }
        return DEFAULT;
    }

    public static Theme themeOf(String color) {
        for (Theme t : values()) {
            if (t.getColorString().equals(color))
                return t;
        }
        return DEFAULT;
    }

    public int getThemeId() { return this.themeId; }

    public String getColorString() { return this.colorString; }

    public int getPrimaryColorId() { return this.primaryColorId; }
}
