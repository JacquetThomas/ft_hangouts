package com.cjacquet.ft.hangouts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String toHoursMinutes(Long time) {
        return new SimpleDateFormat("hh:mm", Locale.US).format(new Date(time));
    }
}
