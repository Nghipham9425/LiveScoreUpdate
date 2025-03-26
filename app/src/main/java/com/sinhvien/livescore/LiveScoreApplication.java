package com.sinhvien.livescore;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class LiveScoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Load saved dark mode preference
        SharedPreferences sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPrefs.getBoolean("dark_mode", false);

        // Apply saved theme setting on app startup
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}