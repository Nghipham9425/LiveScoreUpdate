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
import com.google.firebase.firestore.FirebaseFirestore;

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
            // Lấy tên người dùng từ Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                txtHello.setText("Chào bạn, " + username);
                            } else {
                                txtHello.setText("Chào bạn, User");
                            }
                        } else {
                            // Nếu không có username trong Firestore
                            txtHello.setText("Chào bạn, User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Nếu gặp lỗi khi lấy username
                        txtHello.setText("Chào bạn, User");
                    });

            txtHello.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);  // Hiển thị nút Logout
            btnLogin.setVisibility(View.GONE);     // Ẩn nút Login
            btnJoinNow.setVisibility(View.GONE);   // Ẩn nút Join Now
        } else {
            txtHello.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);   // Ẩn nút Logout
            btnLogin.setVisibility(View.VISIBLE); // Hiển thị nút Login
            btnJoinNow.setVisibility(View.VISIBLE); // Hiển thị nút Join Now
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
