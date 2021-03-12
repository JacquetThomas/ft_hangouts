package com.cjacquet.ft.hangouts.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_THEME_COLOR;
import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_THEME_MODE;

public final class Utils {

    private Utils(){}

    public static String formatNumber(String number) {
        return number.replace("+33", "0");
    }

    public static String formatNumberSearch(String number) {
        return number.replaceAll("^0", "").replace("+33", "");
    }

    public static Theme getTheme(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String modeString = pref.getString(SP_THEME_MODE, null);
        String colorString = pref.getString(SP_THEME_COLOR, null);
        if (modeString != null && colorString != null) {
            return Theme.themeOf(Color.getEnum(colorString), Mode.getEnum(modeString));
        } else if (colorString != null) {
            return Theme.themeOf(Color.getEnum(colorString), Mode.LIGHT);
        } else {
            return Theme.DEFAULT;
        }
    }

    public static String toStringDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String toHoursMinutes(Context context, Long time) {
        Date date = new Date(time);
        if (getLocaleString(context).equalsIgnoreCase(Locale.FRENCH.toString()))
            return new SimpleDateFormat("HH:mm", Locale.FRANCE).format(date);
        else
            return new SimpleDateFormat("hh:mm a", Locale.US).format(date);
    }

    public static String toDay(Context context, Long time) {
        Date date = new Date(time);
        String year = "";

        if (Integer.parseInt(new SimpleDateFormat("yyyy", Locale.FRANCE).format(date))
                < Integer.parseInt(new SimpleDateFormat("yyyy", Locale.FRANCE).format(new Date()))) {
            year = "yyyy";
        }
        if (getLocaleString(context).equalsIgnoreCase(Locale.FRENCH.toString()))
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

    private static String getLocaleString(Context context) {
        return LocaleHelper.getLanguage(context);
    }

    public static boolean getSMSPermission(Context context) {
        return (context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean getCallPermission(Context context) {
        return (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
    }
}
