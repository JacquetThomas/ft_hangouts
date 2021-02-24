package com.cjacquet.ft.hangouts;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.util.Date;

public class BasePausableAppCompatActivity extends AppCompatActivity {
    private Date pausedDate;
    private boolean paused;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paused = false;
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
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toast.show();
        }
        paused = false;
    }
}