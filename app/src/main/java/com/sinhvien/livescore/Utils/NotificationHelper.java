package com.sinhvien.livescore.Utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sinhvien.livescore.Models.MatchNotification;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private static final String PREFS_NAME = "match_notifications";
    private static final String NOTIFICATIONS_KEY = "notifications_list";
    private Context context;

    // Constructor for instance methods
    public NotificationHelper(Context context) {
        this.context = context;
    }

    // Instance method that's causing the NullPointerException
    public void cancelNotification(String matchId) {
        // Add null check to prevent NullPointerException
        if (matchId == null) {
            Log.e("NotificationHelper", "Cannot cancel notification with null matchId");
            return;
        }

        // Cancel notification using NotificationManager
        int notificationId = matchId.hashCode();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }

    // Đặt lịch thông báo
    public static void scheduleNotification(Context context, MatchNotification matchNotification) {
        // Lưu vào SharedPreferences
        saveNotification(context, matchNotification);

        // Đặt lịch thông báo
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        // Thông tin thông báo
        String title = matchNotification.getCompetition();
        String message = matchNotification.getHomeTeam() + " vs " + matchNotification.getAwayTeam();

        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("notificationId", matchNotification.getNotificationId().hashCode());

        // Tạo PendingIntent với ID duy nhất
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                matchNotification.getNotificationId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Đặt thông báo 15 phút trước trận đấu
        long notificationTime = matchNotification.getMatchTime() - (15 * 60 * 1000);

        // Đặt thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    // Hủy thông báo
    public static void cancelNotification(Context context, MatchNotification matchNotification) {
        // Xóa khỏi SharedPreferences
        removeNotification(context, matchNotification);

        // Hủy thông báo đã lên lịch
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                matchNotification.getNotificationId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    // Lưu thông báo vào SharedPreferences
    public static void saveNotification(Context context, MatchNotification notification) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Lấy danh sách thông báo hiện tại
        List<MatchNotification> notifications = getNotifications(context);

        // Kiểm tra đã tồn tại chưa
        boolean exists = false;
        for (MatchNotification n : notifications) {
            if (n.getId().equals(notification.getId())) {
                exists = true;
                break;
            }
        }

        // Thêm nếu chưa tồn tại
        if (!exists) {
            notifications.add(notification);
            String json = gson.toJson(notifications);
            editor.putString(NOTIFICATIONS_KEY, json);
            editor.apply();
        }
    }

    // Xóa thông báo khỏi SharedPreferences
    public static void removeNotification(Context context, MatchNotification notification) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Lấy danh sách thông báo hiện tại
        List<MatchNotification> notifications = getNotifications(context);

        // Xóa thông báo
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(notification.getId())) {
                notifications.remove(i);
                break;
            }
        }

        // Lưu danh sách đã cập nhật
        String json = gson.toJson(notifications);
        editor.putString(NOTIFICATIONS_KEY, json);
        editor.apply();
    }

    // Lấy tất cả thông báo
    public static List<MatchNotification> getNotifications(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(NOTIFICATIONS_KEY, null);

        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<MatchNotification>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // Kiểm tra trận đấu đã đặt thông báo chưa
    public static boolean isMatchScheduled(Context context, String matchId) {
        List<MatchNotification> notifications = getNotifications(context);
        for (MatchNotification notification : notifications) {
            if (notification.getId().equals(matchId)) {
                return true;
            }
        }
        return false;
    }
}