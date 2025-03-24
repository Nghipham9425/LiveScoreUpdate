package com.sinhvien.livescore.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sinhvien.livescore.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserInforActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI components
    private ImageView profileImageView;
    private TextView tvUsername;
    private EditText etEmail, etCurrentPassword, etNewPassword;
    private Button btnSave;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infor);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        profileImageView = findViewById(R.id.profileImageView);
        tvUsername = findViewById(R.id.tvUsername);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSave);

        // Load user data
        loadUserProfile();

        // Set click listeners
        profileImageView.setOnClickListener(v -> openFileChooser());

        btnSave.setOnClickListener(v -> saveChanges());

        // Set up back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Load email
            etEmail.setText(user.getEmail());

            // Set default username
            String defaultUsername = user.getDisplayName() != null ?
                    user.getDisplayName() : "User" + user.getUid().substring(0, 5);
            tvUsername.setText(defaultUsername);

            // Check if the Users collection exists and create user document if needed
            DocumentReference userRef = db.collection("Users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, load data
                        String username = document.getString("username");
                        if (username != null && !username.isEmpty()) {
                            tvUsername.setText(username);
                        }

                        // Try to load avatar from Base64
                        String avatarBase64 = document.getString("avatarBase64");
                        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
                            try {
                                // Remove the prefix if present
                                String base64Data = avatarBase64;
                                if (avatarBase64.contains(",")) {
                                    base64Data = avatarBase64.split(",")[1];
                                }

                                byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                profileImageView.setImageBitmap(decodedBitmap);
                            } catch (Exception e) {
                                // If there's an error, use default image
                                profileImageView.setImageResource(R.drawable.default_profile);
                            }
                        }
                    } else {
                        // Document doesn't exist, create it
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", defaultUsername);
                        userData.put("email", user.getEmail());

                        userRef.set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UserInforActivity.this, "Hồ sơ người dùng đã được tạo", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(UserInforActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(UserInforActivity.this, "Lỗi kết nối đến cơ sở dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Preview the selected image
            Glide.with(this).load(imageUri).into(profileImageView);

            // Upload using Base64 approach
            uploadImageAsBase64();
        }
    }

    private void uploadImageAsBase64() {
        if (imageUri != null) {
            // Show progress dialog
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang xử lý ảnh...");
            progressDialog.show();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String uid = user.getUid();

                try {
                    // Convert image to bitmap with compression
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    // Resize bitmap to reduce storage size (max 200x200 pixels)
                    bitmap = getResizedBitmap(bitmap, 200);

                    // Convert bitmap to Base64 string
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] imageData = baos.toByteArray();
                    String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(imageData, Base64.DEFAULT);

                    // Create or update user document with the Base64 image
                    DocumentReference userRef = db.collection("Users").document(uid);

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("avatarBase64", base64Image);

                    userRef.set(updateData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(UserInforActivity.this,
                                        "Ảnh đại diện đã được cập nhật", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(UserInforActivity.this,
                                        "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } catch (IOException e) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Helper method to resize bitmap
    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void saveChanges() {
        FirebaseUser user = mAuth.getCurrentUser();
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (user != null) {
            // Check if user wants to change password
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Đang cập nhật mật khẩu...");
                progressDialog.show();

                // Properly reauthenticate before password change
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> {
                            // Reauthentication successful, now update password
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(task -> {
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        if (task.isSuccessful()) {
                                            Toast.makeText(UserInforActivity.this, "Mật khẩu đã được cập nhật", Toast.LENGTH_SHORT).show();
                                            etCurrentPassword.setText("");
                                            etNewPassword.setText("");
                                        } else {
                                            Toast.makeText(UserInforActivity.this, "Lỗi: " + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .addOnFailureListener(e -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(UserInforActivity.this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                        });
            } else if (currentPassword.isEmpty() && !newPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
            } else {
                // No password change requested
                Toast.makeText(this, "Không có thay đổi nào được thực hiện", Toast.LENGTH_SHORT).show();
            }
        }
    }
}