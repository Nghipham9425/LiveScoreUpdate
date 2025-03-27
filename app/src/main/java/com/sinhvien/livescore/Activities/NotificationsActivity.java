package com.sinhvien.livescore.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sinhvien.livescore.Adapters.NotificationsAdapter;
import com.sinhvien.livescore.Models.MatchNotification;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Utils.NotificationHelper;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView tvNoNotifications;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);

        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tải danh sách thông báo
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách thông báo khi quay lại màn hình
        loadNotifications();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadNotifications() {
        List<MatchNotification> notifications = NotificationHelper.getNotifications(this);

        if (notifications.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new NotificationsAdapter(this, notifications);
            recyclerView.setAdapter(adapter);
        }
    }
}