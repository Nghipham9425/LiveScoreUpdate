package com.sinhvien.livescore.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinhvien.livescore.Activities.LoginActivity;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Activities.RegisterActivity;

public class SettingFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextView txtHello;
    private Button btnLogin, btnJoinNow, btnLogout, btnNotifications;

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

        updateUI();

        btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        btnJoinNow.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            updateUI();
        });
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
                                txtHello.setText("Chào bạn, " + username);
                            } else {
                                txtHello.setText("Chào bạn, User");
                            }
                        } else {
                            // If no username in Firestore
                            txtHello.setText("Chào bạn, User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If error while fetching username
                        txtHello.setText("Chào bạn, User");
                    });

            txtHello.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);  // Show Logout button
            btnLogin.setVisibility(View.GONE);     // Hide Login button
            btnJoinNow.setVisibility(View.GONE);   // Hide Join Now button
        } else {
            txtHello.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);   // Hide Logout button
            btnLogin.setVisibility(View.VISIBLE); // Show Login button
            btnJoinNow.setVisibility(View.VISIBLE); // Show Join Now button
        }
    }
}
