package com.sinhvien.livescore;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    private View layout;
    private boolean isDefaultColor = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        layout = view.findViewById(R.id.rootLayout);
        Button btnChangeColor = view.findViewById(R.id.btnChangeColor);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnJoinNow = view.findViewById(R.id.btnJoinNow);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnNotifications = view.findViewById(R.id.btnNotifications);

        // Đọc màu nền từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int savedColor = sharedPreferences.getInt("backgroundColor", Color.WHITE);
        layout.setBackgroundColor(savedColor);

        // Sự kiện đổi màu nền
        btnChangeColor.setOnClickListener(v -> {
            int newColor;
            if (isDefaultColor) {
                newColor = Color.parseColor("#979dac");
            } else {
                newColor = Color.WHITE;
            }

            // Lưu màu vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("backgroundColor", newColor);
            editor.apply();


            layout.setBackgroundColor(newColor);
            isDefaultColor = !isDefaultColor;
        });

        return view;
    }
}
