package com.cjacquet.ft.hangouts.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {

    private Utils(){}

    public static String formatNumber(String number) {
        return number.replace("+33", "0");
    }

    public static String toStringDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String stringDate = formatter.format(date);
        return stringDate;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }

    public static boolean getCallPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
        }
        return false;
    }
}
