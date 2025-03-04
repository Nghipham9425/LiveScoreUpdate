package com.sinhvien.livescore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView txtHello;
    private Button btnLogin, btnJoinNow, btnLogout, btnChangeColor, btnNotifications;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        txtHello = view.findViewById(R.id.tvHelloUser);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnJoinNow = view.findViewById(R.id.btnJoinNow);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangeColor = view.findViewById(R.id.btnChangeColor);
        btnNotifications = view.findViewById(R.id.btnNotifications);

        updateUI();

        btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnJoinNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI();
        });

        btnChangeColor.setOnClickListener(v -> {
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", !isDarkMode);
            editor.apply();
            applyTheme();
        });
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            txtHello.setText(username != null && !username.isEmpty() ? "Chào bạn, " + username : "Chào bạn, User");
            txtHello.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnJoinNow.setVisibility(View.GONE);
        } else {
            txtHello.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnJoinNow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        applyTheme();
    }

    private void applyTheme() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        int mode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;

        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode);
            requireActivity().recreate(); // Reload lại Activity để cập nhật theme
        }
    }

}
