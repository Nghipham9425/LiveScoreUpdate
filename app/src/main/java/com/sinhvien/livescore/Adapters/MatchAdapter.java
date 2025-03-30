package com.sinhvien.livescore.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;
import com.sinhvien.livescore.Activities.MatchDetailsActivity;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Models.MatchNotification;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Utils.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final Context context;
    private List<Match> matchList;
    private List<Match> filteredList;
    private Set<String> favoriteMatches = new HashSet<>();
    private FirebaseFirestore db;
    private String uid; // ID người dùng
    private Map<String, String> competitionNames; // Map for competition names

    public MatchAdapter(Context context, List<Match> matches, String uid) {
        this.context = context;
        this.matchList = new ArrayList<>(matches);
        this.filteredList = new ArrayList<>(matches);
        this.db = FirebaseFirestore.getInstance();
        this.uid = uid;

        // Initialize competition names
        initCompetitionNames();

        // Chỉ tải favorites nếu đã đăng nhập
        if (uid != null && !uid.isEmpty()) {
            loadFavoritesFromFirestore();
        }
    }

    private void initCompetitionNames() {
        competitionNames = new HashMap<>();
        competitionNames.put("PL", "Premier League");
        competitionNames.put("CL", "UEFA Champions League");
        competitionNames.put("BL1", "Bundesliga");
        competitionNames.put("SA", "Serie A");
        competitionNames.put("PD", "La Liga");
        competitionNames.put("FL1", "Ligue 1");
        competitionNames.put("EC", "European Championship");
        competitionNames.put("WC", "FIFA World Cup");
        competitionNames.put("ELC", "Championship");
    }

    private String getCompetitionFullName(String code) {
        if (competitionNames.containsKey(code)) {
            return competitionNames.get(code);
        }
        return code; // Return the original code if not found in mapping
    }

    private void loadFavoritesFromFirestore() {
        db.collection("Users").document(uid).collection("favorites")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) return;

                    favoriteMatches.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        favoriteMatches.add(doc.getId()); // Lưu ID trận đấu vào danh sách yêu thích
                    }
                    notifyDataSetChanged();
                });
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = filteredList.get(position);

        holder.tvCompetition.setText(getCompetitionFullName(match.getCompetition()));
        holder.tvScore.setText(match.getScore() != null ? match.getScore() : "-");
        holder.tvHomeTeam.setText(match.getHomeTeam().getShortName() != null ? match.getHomeTeam().getShortName() : match.getHomeTeam().getName());
        holder.tvAwayTeam.setText(match.getAwayTeam().getShortName() != null ? match.getAwayTeam().getShortName() : match.getAwayTeam().getName());
        holder.tvTime.setText(match.getFormattedTime());

        Glide.with(context).load(match.getHomeTeam().getCrest()).into(holder.ivHomeTeam);
        Glide.with(context).load(match.getAwayTeam().getCrest()).into(holder.ivAwayTeam);

        int color;
        switch (match.getStatus().toUpperCase()) {
            case "LIVE":
                color = R.color.status_live;
                break;
            case "FINISHED":
                color = R.color.status_finished;
                break;
            case "UPCOMING":
                color = R.color.status_upcoming;
                break;
            default:
                color = R.color.status_default;
                break;
        }
        holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, color));
        holder.tvStatus.setText(match.getStatus());

        boolean isFavorite = favoriteMatches.contains(match.getMatchId());
        holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_fill : R.drawable.ic_heart_bolder);

        holder.ivFavorite.setOnClickListener(v -> {
            if (uid == null || uid.isEmpty()) {
                Toast.makeText(context, "Bạn cần đăng nhập để lưu yêu thích!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (favoriteMatches.contains(match.getMatchId())) {
                removeFavorite(match.getMatchId(), holder);
            } else {
                addFavorite(match, holder);
            }
        });

        ImageView ivNotification = holder.itemView.findViewById(R.id.ivNotification);

        // Lấy match ID để kiểm tra thông báo
        String matchId = match.getMatchId();

        // Kiểm tra xem trận đấu đã được đặt thông báo chưa
        boolean isScheduled = NotificationHelper.isMatchScheduled(context, matchId);

        // Cập nhật icon thông báo tương ứng
        ivNotification.setImageResource(isScheduled ?
                R.drawable.ic_notification_on : R.drawable.ic_notification_off);

        // Xử lý khi nhấn vào icon chuông
        ivNotification.setOnClickListener(v -> {
            if (isScheduled) {
                // Hủy thông báo
                MatchNotification notification = new MatchNotification();
                notification.setId(matchId);
                NotificationHelper.cancelNotification(context, notification);

                // Cập nhật giao diện
                ivNotification.setImageResource(R.drawable.ic_notification_off);
                Toast.makeText(context, "Đã hủy thông báo trận đấu", Toast.LENGTH_SHORT).show();
            } else {
                // Đặt thông báo mới
                String homeTeam = match.getHomeTeam().getName();
                String awayTeam = match.getAwayTeam().getName();
                String competition = getCompetitionFullName(match.getCompetition());

                // Chuyển đổi thời gian trận đấu
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                long matchTimeMillis = 0;
                try {
                    Date matchDate = sdf.parse(match.getMatchTime());
                    matchTimeMillis = matchDate.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Chỉ tiếp tục nếu chuyển đổi thời gian thành công
                if (matchTimeMillis > 0) {
                    // Tạo đối tượng thông báo
                    MatchNotification notification = new MatchNotification(
                            matchId,
                            homeTeam,
                            awayTeam,
                            competition,
                            matchTimeMillis
                    );

                    // Đặt lịch thông báo
                    NotificationHelper.scheduleNotification(context, notification);

                    // Cập nhật giao diện
                    ivNotification.setImageResource(R.drawable.ic_notification_on);
                    Toast.makeText(context, "Sẽ thông báo trước khi trận đấu bắt đầu", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Không thể đặt thông báo, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MatchDetailsActivity.class);
            intent.putExtra("match_id", match.getMatchId());
            context.startActivity(intent);
        });
    }

    private void addFavorite(Match match, MatchViewHolder holder) {
        db.collection("Users").document(uid).collection("favorites")
                .document(match.getMatchId())
                .set(match)
                .addOnSuccessListener(aVoid -> {
                    favoriteMatches.add(match.getMatchId());
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_fill);
                    Toast.makeText(context, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi thêm yêu thích!", Toast.LENGTH_SHORT).show());
    }

    private void removeFavorite(String matchId, MatchViewHolder holder) {
        db.collection("Users").document(uid).collection("favorites")
                .document(matchId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật danh sách yêu thích trong bộ nhớ đệm
                    favoriteMatches.remove(matchId);
                    holder.ivFavorite.setImageResource(R.drawable.ic_heart_bolder);
                    Toast.makeText(context, "Đã xóa khỏi yêu thích!", Toast.LENGTH_SHORT).show();
                    // Cập nhật giao diện sau khi xóa
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa yêu thích!", Toast.LENGTH_SHORT).show();
                    Log.e("Remove Favorite", "Error removing favorite: " + e.getMessage());
                });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateData(List<Match> newMatches) {
        this.matchList = new ArrayList<>(newMatches);
        this.filteredList = new ArrayList<>(newMatches);
        notifyDataSetChanged();
    }

    public void filterMatches(String query) {
        query = query.toLowerCase().trim();
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(matchList);
        } else {
            for (Match match : matchList) {
                if (match.getHomeTeam().getName().toLowerCase().contains(query) ||
                        match.getAwayTeam().getName().toLowerCase().contains(query)) {
                    filteredList.add(match);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompetition, tvScore, tvHomeTeam, tvAwayTeam, tvTime, tvStatus;
        ImageView ivHomeTeam, ivAwayTeam, ivFavorite;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCompetition = itemView.findViewById(R.id.tvCompetition);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvHomeTeam = itemView.findViewById(R.id.tvHomeTeam);
            tvAwayTeam = itemView.findViewById(R.id.tvAwayTeam);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivHomeTeam = itemView.findViewById(R.id.ivHomeTeam);
            ivAwayTeam = itemView.findViewById(R.id.ivAwayTeam);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}