package com.cjacquet.ft.hangouts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String toHoursMinutes(Long time) {
        if (LocaleHelper.getLanguage(CatalogActivity.getContext()).equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("HH:mm", Locale.FRANCE).format(new Date(time));
        else
            return new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(time));
    }

    public static String toDay(Long time) {
        if (LocaleHelper.getLanguage(CatalogActivity.getContext()).equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("EEE d MMM", Locale.FRANCE).format(new Date(time));
        else
            return new SimpleDateFormat("EEE, MMM d", Locale.US).format(new Date(time));
    }
}
