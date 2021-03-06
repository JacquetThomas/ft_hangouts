package com.cjacquet.ft.hangouts.utils;

import com.cjacquet.ft.hangouts.R;

public enum Theme {
    DEFAULT(R.style.AppTheme, R.color.colorPrimary, R.color.colorPrimaryDark, "orange"),
    ORANGE(R.style.OrangeTheme, R.color.orangeColorPrimary, R.color.orangeColorPrimaryDark, "orange"),
    BLUE(R.style.BlueTheme, R.color.blueColorPrimary, R.color.blueColorPrimaryDark, "blue"),
    GREEN(R.style.GreenTheme, R.color.greenColorPrimary, R.color.greenColorPrimaryDark, "green"),
    ORANGE_DARK(R.style.OrangeThemeDark, R.color.orangeColorPrimary, R.color.orangeColorPrimaryLight, "orangeDark"),
    BLUE_DARK(R.style.BlueThemeDark, R.color.blueColorPrimary, R.color.blueColorPrimaryLight, "blueDark"),
    GREEN_DARK(R.style.GreenThemeDark, R.color.greenColorPrimary, R.color.greenColorPrimaryLight, "greenDark");

    private final int themeId;
    private final int primaryColorId;
    private final int primaryDarkColorId;
    private final String colorString;

    Theme(final int value, final int primaryColorId, final int primaryDarkColorId, final String colorString) {
        this.themeId = value;
        this.primaryColorId = primaryColorId;
        this.primaryDarkColorId = primaryDarkColorId;
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

    public static Theme oppositeOf(Theme theme) {
        switch (theme) {
            case ORANGE:
                return ORANGE_DARK;
            case BLUE:
                return BLUE_DARK;
            case GREEN:
                return GREEN_DARK;
            case ORANGE_DARK:
                return ORANGE;
            case BLUE_DARK:
                return BLUE;
            case GREEN_DARK:
                return GREEN;
        }
        return DEFAULT;
    }

    public int getThemeId() { return this.themeId; }

    public String getColorString() { return this.colorString; }

    public int getPrimaryColorId() { return this.primaryColorId; }
    public int getPrimaryDarkColorId() { return this.primaryDarkColorId; }
}
