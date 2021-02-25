package com.cjacquet.ft.hangouts;

public enum Theme {
    DEFAULT(R.style.AppTheme, R.color.colorPrimary),
    ORANGE(R.style.OrangeTheme, R.color.orangeColorPrimary),
    BLUE(R.style.BlueTheme, R.color.blueColorPrimary),
    GREEN(R.style.GreenTheme, R.color.greenColorPrimary);

    private final int themeId;
    private final int primaryColorId;

    Theme(final int value, final int primaryColorId) {
        this.themeId = value;
        this.primaryColorId = primaryColorId;
    }

    public static Theme valueOf(int themeId) {
        for (Theme t : values()) {
            if (t.getThemeId() == themeId)
                return t;
        }
        return DEFAULT;
    }

    public int getThemeId() { return this.themeId; }

    public int getPrimaryColorId() { return this.primaryColorId; }
}
