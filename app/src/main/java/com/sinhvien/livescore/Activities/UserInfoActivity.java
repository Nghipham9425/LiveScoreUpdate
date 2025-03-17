package com.sinhvien.livescore.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinhvien.livescore.R;

public class UserInfoActivity extends AppCompatActivity {

    private static final String TAG = "UserInfoActivity";
    private TextView tvUsername;
    private TextView tvEmail; // Chuyển từ EditText sang TextView
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infor);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.etEmail); // Vẫn dùng id cũ
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Load user data
        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Set email from Firebase Auth
            tvEmail.setText(user.getEmail());

            // Get username from Firestore
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            if (username != null && !username.isEmpty()) {
                                tvUsername.setText(username);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user data", e));
        }
    }

    private void saveUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Cần nhập mật khẩu hiện tại");
            return;
        }

        // Check for changes - chỉ kiểm tra thay đổi mật khẩu
        boolean hasPasswordChange = !TextUtils.isEmpty(newPassword);

        if (!hasPasswordChange) {
            Toast.makeText(this, "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        // Re-authenticate user before making changes
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Xác thực người dùng thành công");

                    // Chỉ cập nhật mật khẩu
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> {
                                Log.d(TAG, "Cập nhật mật khẩu thành công");
                                etCurrentPassword.setText("");
                                etNewPassword.setText("");

                                Toast.makeText(UserInfoActivity.this,
                                        "Mật khẩu đã được cập nhật thành công!",
                                        Toast.LENGTH_SHORT).show();

                                btnSave.setEnabled(true);
                                btnSave.setText("Lưu Thay Đổi");
                                finish(); // Quay lại màn hình trước
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Lỗi cập nhật mật khẩu", e);
                                Toast.makeText(UserInfoActivity.this,
                                        "Không thể cập nhật mật khẩu: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                btnSave.setEnabled(true);
                                btnSave.setText("Lưu Thay Đổi");
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Xác thực lại thất bại", e);
                    Toast.makeText(UserInfoActivity.this,
                            "Xác thực thất bại: Mật khẩu không đúng",
                            Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    btnSave.setText("Lưu Thay Đổi");
                });
    }
}