package com.sinhvien.livescore.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sinhvien.livescore.Activities.NotificationsActivity;
import com.sinhvien.livescore.Models.MatchNotification;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {
    private Context context;
    private List<MatchNotification> notifications;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificationsAdapter(Context context, List<MatchNotification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        MatchNotification notification = notifications.get(position);

        holder.tvMatch.setText(notification.getHomeTeam() + " vs " + notification.getAwayTeam());
        holder.tvCompetition.setText(notification.getCompetition());

        // Định dạng thời gian trận đấu
        Date matchDate = new Date(notification.getMatchTime());
        holder.tvTime.setText(dateFormat.format(matchDate));

        // Xử lý nút hủy thông báo
        holder.btnCancel.setOnClickListener(v -> {
            NotificationHelper.cancelNotification(context, notification);

            // Xóa khỏi danh sách và cập nhật adapter
            notifications.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notifications.size());

            // Kiểm tra xem danh sách có trống không
            if (notifications.isEmpty()) {
                // Gọi phương thức ở Activity để hiện thông báo trống
                ((NotificationsActivity)context).loadNotifications();
            }

            // Hiện thông báo cho người dùng
            Toast.makeText(context, "Đã hủy thông báo trận đấu", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatch, tvCompetition, tvTime;
        Button btnCancel;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMatch = itemView.findViewById(R.id.tvMatch);
            tvCompetition = itemView.findViewById(R.id.tvCompetition);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnCancel = itemView.findViewById(R.id.btnCancelNotification);
        }
    }
}