package com.sinhvien.livescore.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinhvien.livescore.Activities.LoginActivity;
import com.sinhvien.livescore.Activities.NotificationsActivity;
import com.sinhvien.livescore.Activities.UserInforActivity;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Activities.RegisterActivity;

public class SettingFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView txtHello;
    private Button btnLogin, btnJoinNow, btnLogout, btnNotifications;
    private MaterialButton btnUserInfo;

    // Dark mode components
    private LinearLayout darkModeToggle;
    private SwitchCompat switchDarkMode;
    private ImageView imgThemeIcon;
    private SharedPreferences sharedPrefs;

    // Social media icons
    private ImageView imgInstagram, imgFacebook, imgYoutbe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        txtHello = view.findViewById(R.id.tvHelloUser);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnJoinNow = view.findViewById(R.id.btnJoinNow);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnUserInfo = view.findViewById(R.id.btnUserInfo);

        // Initialize dark mode components
        darkModeToggle = view.findViewById(R.id.darkModeToggle);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        imgThemeIcon = view.findViewById(R.id.imgThemeIcon);

        // Initialize social media icons
        imgInstagram = view.findViewById(R.id.imgInstagram);
        imgFacebook = view.findViewById(R.id.imgFacebook);
        imgYoutbe = view.findViewById(R.id.imgYoutbe);

        // Set click listeners for social media icons
        imgInstagram.setOnClickListener(v -> openUrl("https://www.instagram.com/huflit.official"));
        imgFacebook.setOnClickListener(v -> openUrl("https://www.facebook.com/huflit.edu.vn"));
        imgYoutbe.setOnClickListener(v -> openUrl("https://www.youtube.com/@huflitofficial2711"));

        // Initialize shared preferences
        sharedPrefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Read saved dark mode preference first, then check current mode as fallback
        boolean isDarkMode = sharedPrefs.getBoolean("dark_mode",
                AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

        // Set initial state for dark mode switch
        switchDarkMode.setChecked(isDarkMode);
        updateThemeIcon(isDarkMode);

        // Set listeners for dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                setDarkMode(isChecked);
            }
        });

        darkModeToggle.setOnClickListener(v -> {
            boolean newState = !switchDarkMode.isChecked();
            switchDarkMode.setChecked(newState);
            setDarkMode(newState);
        });

        updateUI();

        btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnJoinNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterActivity.class)));

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI();
        });

        btnUserInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserInforActivity.class);
            startActivity(intent);
        });
    }

    // Helper method to open URLs
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update UI when returning to the fragment
        updateUI();

        // Ensure the switch is in sync with the current theme
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        switchDarkMode.setChecked(isDarkMode);
        updateThemeIcon(isDarkMode);
    }

    private void setDarkMode(boolean isDarkMode) {
        // Save preference
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean("dark_mode", isDarkMode);
        editor.apply();

        // Update theme icon
        updateThemeIcon(isDarkMode);

        // Apply theme
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Recreate the activity to apply the theme change immediately
        requireActivity().recreate();
    }

    private void updateThemeIcon(boolean isDarkMode) {
        imgThemeIcon.setImageResource(isDarkMode ? R.drawable.dark_ic : R.drawable.light_ic);
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Get username from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                txtHello.setText("Hello, " + username);
                            } else {
                                txtHello.setText("Hello, User");
                            }
                        } else {
                            // If no username in Firestore
                            txtHello.setText("Hello, User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If error while fetching username
                        txtHello.setText("Hello, User");
                    });

            txtHello.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);  // Show Logout button
            btnLogin.setVisibility(View.GONE);     // Hide Login button
            btnJoinNow.setVisibility(View.GONE);   // Hide Join Now button
            btnUserInfo.setVisibility(View.VISIBLE); // Show User Info button when logged in
        } else {
            txtHello.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);   // Hide Logout button
            btnLogin.setVisibility(View.VISIBLE); // Show Login button
            btnJoinNow.setVisibility(View.VISIBLE); // Show Join Now button
            btnUserInfo.setVisibility(View.GONE); // Hide User Info button when not logged in
        }
    }
}