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
import androidx.appcompat.app.AppCompatActivity;
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
        sharedPreferences = getActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE); // Khởi tạo SharedPreferences
        txtHello = view.findViewById(R.id.tvHelloUser);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnJoinNow = view.findViewById(R.id.btnJoinNow);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangeColor = view.findViewById(R.id.btnChangeColor);
        btnNotifications = view.findViewById(R.id.btnNotifications);

        // Cập nhật giao diện dựa trên việc người dùng có đăng nhập hay không
        updateUI();

        // Sự kiện nút
        btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnJoinNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI(); // Cập nhật giao diện sau khi đăng xuất
        });

        // Chuyển đổi giữa chế độ sáng và tối
        btnChangeColor.setOnClickListener(v -> {
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false); // Kiểm tra trạng thái chế độ tối
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_mode", !isDarkMode); // Chuyển đổi chế độ
            editor.apply();
            applyTheme(); // Áp dụng chế độ mới ngay lập tức
        });

        btnNotifications.setOnClickListener(v -> {
            // Xử lý thiết lập thông báo
        });
    }

    private void updateUI() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String username = user.getDisplayName();
            if (username != null && !username.isEmpty()) {
                txtHello.setText("Chào bạn, " + username);
            } else {
                txtHello.setText("Chào bạn, User");
            }
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
        applyTheme(); // Đảm bảo áp dụng đúng chế độ khi Fragment được hiển thị lại
    }

    private void applyTheme() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Chế độ tối
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // Chế độ sáng
        }
    }
}
