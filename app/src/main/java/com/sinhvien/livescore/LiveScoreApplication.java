package com.sinhvien.livescore;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

public class LiveScoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Tạo notification channel cho Android 8.0+
        createNotificationChannel();

        // Đọc chế độ tối từ SharedPreferences
        SharedPreferences sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPrefs.getBoolean("dark_mode", false);

        // Áp dụng chế độ tối khi khởi động
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "match_channel",
                    "Thông báo trận đấu",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi trận đấu sắp diễn ra");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}