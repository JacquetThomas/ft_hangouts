package com.cjacquet.ft.hangouts.utils;

public enum Color {
    ORANGE, BLUE, GREEN;

    public static Color getEnum(String Color) {
        for (Color c : values()) {
            if (c.toString().equalsIgnoreCase(Color))
                return c;
        }
        return ORANGE;
    }

    }
