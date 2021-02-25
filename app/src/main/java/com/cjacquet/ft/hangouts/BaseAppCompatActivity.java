package com.cjacquet.ft.hangouts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.cjacquet.ft.hangouts.utils.Theme;
import com.cjacquet.ft.hangouts.utils.Utils;

import java.util.Date;

public class BaseAppCompatActivity extends AppCompatActivity {
    private static final String SP_THEME_COLOR_ID = "colorThemeId";
    private Date pausedDate;
    private boolean paused;
    public static Theme colorTheme = Theme.ORANGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
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

    @Override
    protected void onPause() {
        super.onPause();
        pausedDate = new Date();
        paused = true;
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            String text = getResources().getString(R.string.on_resume_text) + Utils.toHoursMinutes(pausedDate.getTime());
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        }
        paused = false;
    }
}