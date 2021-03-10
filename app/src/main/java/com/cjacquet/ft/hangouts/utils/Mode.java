package com.cjacquet.ft.hangouts.utils;

public enum Mode {
    LIGHT, DARK;

    public static Mode getEnum(String mode) {
        for (Mode m : values()) {
            if (m.toString().equalsIgnoreCase(mode))
                return m;
        }
        return LIGHT;
    }
}
