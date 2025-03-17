package com.sinhvien.livescore.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.R;
import java.util.*;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final Context context;
    private List<Match> matchList;
    private List<Match> filteredList;
    private Set<String> favoriteMatches = new HashSet<>();
    private FirebaseFirestore db;
    private String uid; // ID người dùng

    public MatchAdapter(Context context, List<Match> matches, String uid) {
        this.context = context;
        this.matchList = new ArrayList<>(matches);
        this.filteredList = new ArrayList<>(matches);
        this.db = FirebaseFirestore.getInstance();
        this.uid = uid;

        // Chỉ tải favorites nếu đã đăng nhập
        if (uid != null && !uid.isEmpty()) {
            loadFavoritesFromFirestore();
        }
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

        holder.tvCompetition.setText(match.getCompetition());
        holder.tvScore.setText(match.getScore() != null ? match.getScore() : "-");
        holder.tvHomeTeam.setText(match.getHomeTeam().getName());
        holder.tvAwayTeam.setText(match.getAwayTeam().getName());
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

        // Xử lý sự kiện ấn vào trái tim
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

        public MatchViewHolder(View itemView) {
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
