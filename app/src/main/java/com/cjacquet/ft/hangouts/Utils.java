package com.cjacquet.ft.hangouts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {
    private static String getLocaleString() {
        return LocaleHelper.getLanguage(CatalogActivity.getContext());
    }

    public static String toHoursMinutes(Long time) {
        Date date = new Date(time);
        if (getLocaleString().equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("HH:mm", Locale.FRANCE).format(date);
        else
            return new SimpleDateFormat("hh:mm a", Locale.US).format(date);
    }

    public static String toDay(Long time) {
        Date date = new Date(time);
        String year = "";

        if (Integer.parseInt(new SimpleDateFormat("yyyy", Locale.FRANCE).format(date))
                < Integer.parseInt(new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date()))) {
            year = "yyyy";
        }
        if (getLocaleString().equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("EEE d MMM " + year, Locale.FRANCE).format(date);
        else
            return new SimpleDateFormat("EEE, MMM d " + year, Locale.US).format(date);
    }

    public static String setFormattedDate(int year, int monthOfYear, int dayOfMonth) {
        String extraZeroMonth = "";
        String extraZeroDay = "";
        if (++monthOfYear < 10)
            extraZeroMonth = "0";
        if (dayOfMonth < 10)
            extraZeroDay = "0";
        return extraZeroDay + dayOfMonth + "/" + extraZeroMonth + monthOfYear + "/" + year;
    }
}
