package com.sinhvien.livescore.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.sinhvien.livescore.R;

public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView loadingTextView;
    private int progressStatus = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // First, request window features
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Then handle splash screen API
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
            splashScreen.setKeepOnScreenCondition(() -> false);
        }

        super.onCreate(savedInstanceState);

        // Set window flags
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Make window transparent to avoid default splash screen flash
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.activity_splash);

        // Initialize progress bar
        progressBar = findViewById(R.id.progressBar);
        loadingTextView = findViewById(R.id.loadingTextView);
        startLoadingAnimation();
    }

    private void startLoadingAnimation() {
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;

                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    if (progressStatus < 30) {
                        loadingTextView.setText("Loading data...");
                    } else if (progressStatus < 70) {
                        loadingTextView.setText("Preparing app...");
                    } else {
                        loadingTextView.setText("Almost there...");
                    }
                });

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Navigate to main activity when complete
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).start();
    }
}