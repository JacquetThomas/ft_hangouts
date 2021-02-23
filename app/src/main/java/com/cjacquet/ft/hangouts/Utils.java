package com.cjacquet.ft.hangouts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String toHoursMinutes(Long time) {
        Date date = new Date(time);
        if (LocaleHelper.getLanguage(CatalogActivity.getContext()).equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("HH:mm", Locale.FRANCE).format(date);
        else
            return new SimpleDateFormat("hh:mm a", Locale.US).format(date);
    }

    public static String toDay(Long time) {
        Date date = new Date(time);
        String year = "";

        if (Integer.valueOf(new SimpleDateFormat("yyyy", Locale.FRANCE).format(date)) < Integer.valueOf(new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date())))
            year = "yyyy";
        if (LocaleHelper.getLanguage(CatalogActivity.getContext()).equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("EEE d MMM " + year, Locale.FRANCE).format(date);
        else
            return new SimpleDateFormat("EEE, MMM d " + year, Locale.US).format(date);
    }
}
