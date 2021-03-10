package com.cjacquet.ft.hangouts.utils;

import com.cjacquet.ft.hangouts.R;

public enum Theme {
    DEFAULT(R.style.AppTheme, R.color.colorPrimary, R.color.colorPrimaryDark, Color.ORANGE, Mode.LIGHT),
    ORANGE(R.style.OrangeTheme, R.color.orangeColorPrimary, R.color.orangeColorPrimaryDark, Color.ORANGE, Mode.LIGHT),
    BLUE(R.style.BlueTheme, R.color.blueColorPrimary, R.color.blueColorPrimaryDark, Color.BLUE, Mode.LIGHT),
    GREEN(R.style.GreenTheme, R.color.greenColorPrimary, R.color.greenColorPrimaryDark, Color.GREEN, Mode.LIGHT),
    ORANGE_DARK(R.style.OrangeThemeDark, R.color.orangeColorPrimary, R.color.orangeColorPrimaryLight, Color.ORANGE, Mode.DARK),
    BLUE_DARK(R.style.BlueThemeDark, R.color.blueColorPrimary, R.color.blueColorPrimaryLight, Color.BLUE, Mode.DARK),
    GREEN_DARK(R.style.GreenThemeDark, R.color.greenColorPrimary, R.color.greenColorPrimaryLight, Color.GREEN, Mode.DARK);

    private final int themeId;
    private final int primaryColorId;
    private final int primaryDarkColorId;
    private final Color color;
    private final Mode mode;

    Theme(final int value, final int primaryColorId, final int primaryDarkColorId, final Color colorString, final Mode mode) {
        this.themeId = value;
        this.primaryColorId = primaryColorId;
        this.primaryDarkColorId = primaryDarkColorId;
        this.color = colorString;
        this.mode = mode;
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
            if (t.getColor().equals(color))
                return t;
        }
        return DEFAULT;
    }

    public static Theme themeOf(Color color, Mode mode) {
        for (Theme t : values()) {
            if (t.getColor().equals(color) && t.getMode().equals(mode))
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

    public Color getColor() { return this.color; }
    public Mode getMode() {return this.mode; }
    public int getPrimaryColorId() { return this.primaryColorId; }
    public int getPrimaryDarkColorId() { return this.primaryDarkColorId; }
}
