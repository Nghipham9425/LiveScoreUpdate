package com.sinhvien.livescore;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class LiveScoreApplication extends Application {
    private static final String TAG = "LiveScoreApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        initializeFirebase();

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

    private void initializeFirebase() {
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);

            // Configure Firestore
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            FirebaseFirestore.getInstance().setFirestoreSettings(settings);

            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
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