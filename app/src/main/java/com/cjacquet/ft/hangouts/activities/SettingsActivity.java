package com.cjacquet.ft.hangouts.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cjacquet.ft.hangouts.R;

import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_PREF_LANG;
import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_THEME_COLOR;
import static com.cjacquet.ft.hangouts.utils.SharedPreferencesConstant.SP_THEME_MODE;

public class SettingsActivity extends BaseAppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        setTitle(R.string.settings_activity_title);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                if (key.equals(SP_PREF_LANG) || key.equals(SP_THEME_COLOR) || key.equals(SP_THEME_MODE)) {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener);
                    Intent i = getIntent();
                    finish();
                    startActivity(i);
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference pref = findPreference(SP_THEME_MODE);
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(SP_THEME_MODE, newValue.toString());
                    editor.apply();
                    return true;
                }
            });
        }
    }
}