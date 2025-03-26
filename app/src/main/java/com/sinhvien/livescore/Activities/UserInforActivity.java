package com.sinhvien.livescore.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInforActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private CircleImageView profileImageView;
    private TextView tvUsername;
    private TextInputEditText etEmail, etCurrentPassword, etNewPassword;
    private MaterialButton btnSave;
    private ImageView btnBack;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_infor);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Khởi tạo view
        profileImageView = findViewById(R.id.profileImageView);
        tvUsername = findViewById(R.id.tvUsername);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        // Set listeners
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveChanges());
        findViewById(R.id.fabChangePhoto).setOnClickListener(v -> openFileChooser());

        // Tải dữ liệu người dùng
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Tải email
            etEmail.setText(user.getEmail());

            // Đặt tên người dùng mặc định
            String defaultUsername = user.getDisplayName() != null ?
                    user.getDisplayName() : "User" + user.getUid().substring(0, 5);
            tvUsername.setText(defaultUsername);

            // Kiểm tra bộ sưu tập Users và tạo tài liệu người dùng nếu cần
            DocumentReference userRef = db.collection("Users").document(user.getUid());
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Tài liệu tồn tại, tải dữ liệu
                        String username = document.getString("username");
                        if (username != null && !username.isEmpty()) {
                            tvUsername.setText(username);
                        }

                        // Thử tải ảnh chất lượng cao trước
                        String avatarHighQuality = document.getString("avatarHighQuality");
                        if (avatarHighQuality != null && !avatarHighQuality.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(avatarHighQuality, Base64.DEFAULT);

                                // Sử dụng Glide để tải ảnh từ mảng byte
                                Glide.with(this)
                                        .load(decodedString)
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Bật cache
                                        .dontAnimate() // Tránh animation làm giảm chất lượng
                                        .into(profileImageView);
                            } catch (Exception e) {
                                // Thử các định dạng khác nếu không đọc được
                                loadWebPAvatar(document);
                            }
                        } else {
                            // Thử các định dạng khác
                            loadWebPAvatar(document);
                        }
                    } else {
                        // Tài liệu không tồn tại, tạo nó
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("username", defaultUsername);
                        userData.put("email", user.getEmail());

                        userRef.set(userData)
                                .addOnSuccessListener(aVoid -> Toast.makeText(UserInforActivity.this, "Hồ sơ người dùng đã được tạo", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(UserInforActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(UserInforActivity.this, "Lỗi kết nối đến cơ sở dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadWebPAvatar(DocumentSnapshot document) {
        // Thử tải ảnh WebP tối ưu
        String avatarWebP = document.getString("avatarWebP");
        if (avatarWebP != null && !avatarWebP.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(avatarWebP, Base64.DEFAULT);

                // Sử dụng Glide để tải ảnh từ mảng byte
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImageView);
            } catch (Exception e) {
                // Nếu có lỗi, kiểm tra định dạng legacy
                loadLegacyAvatar(document);
            }
        } else {
            // Nếu không có định dạng WebP, thử định dạng legacy
            loadLegacyAvatar(document);
        }
    }

    private void loadLegacyAvatar(DocumentSnapshot document) {
        // Thử tải ảnh từ Base64 (định dạng cũ)
        String avatarBase64 = document.getString("avatarBase64");
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            try {
                // Loại bỏ tiền tố nếu có
                String base64Data = avatarBase64;
                if (avatarBase64.contains(",")) {
                    base64Data = avatarBase64.split(",")[1];
                }

                byte[] decodedString = Base64.decode(base64Data, Base64.DEFAULT);

                // Sử dụng Glide để cải thiện hiệu suất và chất lượng
                Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(profileImageView);
            } catch (Exception e) {
                // Nếu có lỗi, sử dụng hình ảnh mặc định
                profileImageView.setImageResource(R.drawable.default_profile);
            }
        } else {
            // Không tìm thấy ảnh đại diện, sử dụng ảnh mặc định
            profileImageView.setImageResource(R.drawable.default_profile);
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Hiển thị xem trước
            Glide.with(this).load(imageUri).into(profileImageView);

            // Sử dụng phương thức mới với chất lượng cao hơn
            uploadHighQualityImageToFirestore();
        }
    }

    private void uploadHighQualityImageToFirestore() {
        if (imageUri != null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang xử lý ảnh...");
            progressDialog.show();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                try {
                    // 1. Tải bitmap chất lượng cao với options
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream, null, options);
                    imageStream.close();

                    // 2. Tập trung vào khuôn mặt nếu có thể (ảnh đại diện)
                    // Crop thành hình vuông từ trung tâm
                    Bitmap squareBitmap = cropToSquare(originalBitmap);

                    // 3. Resize với độ phân giải cao hơn (600px cho avatar)
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, 600, 600, true);

                    // 4. Sử dụng PNG cho chất lượng tốt nhất nếu kích thước đủ nhỏ
                    ByteArrayOutputStream baosPNG = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baosPNG);

                    // Kiểm tra kích thước, nếu quá lớn thì dùng WebP chất lượng cao
                    byte[] imageData = baosPNG.toByteArray();
                    if (imageData.length > 500000) { // 500KB
                        // Sử dụng WebP chất lượng cao hơn (90%)
                        ByteArrayOutputStream baosWebP = new ByteArrayOutputStream();
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                            resizedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, baosWebP);
                        } else {
                            resizedBitmap.compress(Bitmap.CompressFormat.WEBP, 90, baosWebP);
                        }
                        imageData = baosWebP.toByteArray();
                    }

                    String base64Image = Base64.encodeToString(imageData, Base64.NO_WRAP);

                    // Lưu vào Firestore với định dạng tối ưu hơn
                    DocumentReference userRef = db.collection("Users").document(user.getUid());
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("avatarHighQuality", base64Image);

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

    // Phương thức cắt ảnh thành hình vuông từ trung tâm
    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        int x = (width - size) / 2;
        int y = (height - size) / 2;

        return Bitmap.createBitmap(bitmap, x, y, size, size);
    }

    // Phương thức thay đổi kích thước bitmap
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
            // Kiểm tra nếu người dùng muốn thay đổi mật khẩu
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Đang cập nhật mật khẩu...");
                progressDialog.show();

                // Xác thực lại trước khi thay đổi mật khẩu
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> {
                            // Xác thực thành công, cập nhật mật khẩu
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
                // Không có yêu cầu thay đổi mật khẩu
                Toast.makeText(this, "Không có thay đổi nào được thực hiện", Toast.LENGTH_SHORT).show();
            }
        }
    }
}