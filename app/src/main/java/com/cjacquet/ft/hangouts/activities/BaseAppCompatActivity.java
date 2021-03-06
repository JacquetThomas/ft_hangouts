package com.cjacquet.ft.hangouts.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cjacquet.ft.hangouts.R;
import com.cjacquet.ft.hangouts.receiver.SmsReceiver;
import com.cjacquet.ft.hangouts.utils.LocaleHelper;
import com.cjacquet.ft.hangouts.utils.Theme;
import com.cjacquet.ft.hangouts.utils.Utils;

import java.util.Date;

public class BaseAppCompatActivity extends AppCompatActivity {
    private static final String SP_THEME_COLOR_ID = "colorThemeId";
    private static final String SP_PREF_LANG = "prefLang";
    private static final String SP_WLC_MSG = "welcomeMessage";
    private Date pausedDate;
    private boolean paused;
    private static Theme colorTheme = Theme.ORANGE;
    private SmsReceiver receiver = new SmsReceiver();

    public static String getSpPrefLang() {
        return SP_PREF_LANG;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = pref.getString(SP_PREF_LANG, null);
        if (lang != null) {
            LocaleHelper.setLocale(this, lang);
        } else {
            LocaleHelper.setLocale(this, getResources().getConfiguration().locale.getLanguage());
        }
        paused = false;
        int themeSaved = pref.getInt(SP_THEME_COLOR_ID, -1);
        if (themeSaved != 0) {
            colorTheme = Theme.valueOf(themeSaved);
        }
        setTheme(colorTheme.getThemeId());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setTheme(int resId) {
        colorTheme = Theme.valueOf(resId);
        if (resId != Theme.DEFAULT.getThemeId()) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(SP_THEME_COLOR_ID, colorTheme.getThemeId());
            editor.apply();
        }
        super.setTheme(resId);
    }

    public static Theme getColorTheme() {
        return colorTheme;
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausedDate = new Date();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String welcome = pref.getString(SP_WLC_MSG, null);
            String text = welcome + "\n" + getResources().getString(R.string.base_activity_on_resume_text) + Utils.toHoursMinutes(getApplicationContext(), pausedDate.getTime());
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        }
        paused = false;
    }

    @Override
    protected void onStop() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SP_PREF_LANG, getResources().getConfiguration().locale.getLanguage());
        editor.apply();
        super.onStop();
    }
}