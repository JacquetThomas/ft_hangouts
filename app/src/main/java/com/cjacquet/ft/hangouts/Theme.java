package com.cjacquet.ft.hangouts;

public enum Theme {
    ORANGE(R.style.OrangeTheme), BLUE(R.style.BlueTheme), GREEN(R.style.GreenTheme);

    private final int value;

    Theme(final int value) {
        this.value = value;
    }

    public int getValue() { return this.value; }
}
