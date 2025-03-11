package com.sinhvien.livescore.Adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.R;

import java.util.*;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final Context context;
    private final List<Match> originalMatches; // Danh sách gốc
    private List<Match> filteredMatches; // Danh sách đã lọc

    public MatchAdapter(Context context, List<Match> matches) {
        this.context = context;
        this.originalMatches = new ArrayList<>(matches);
        this.filteredMatches = new ArrayList<>(matches);
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = filteredMatches.get(position);

        holder.tvCompetition.setText(match.getCompetition());
        holder.tvScore.setText(match.getScore());
        holder.tvHomeTeam.setText(match.getHomeTeam().getName());
        holder.tvAwayTeam.setText(match.getAwayTeam().getName());
        holder.tvTime.setText(match.getMatchTime());
        holder.tvStatus.setText(match.getStatus());

        Glide.with(context).load(match.getHomeTeam().getCrest()).into(holder.ivHomeTeam);
        Glide.with(context).load(match.getAwayTeam().getCrest()).into(holder.ivAwayTeam);

        switch (match.getStatus().toUpperCase()) {
            case "LIVE":
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_live));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                break;
            case "FINISHED":
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_finished));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                break;
            case "UPCOMING":
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_upcoming));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                break;
            default:
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.status_default));
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return filteredMatches.size();
    }

    // 🔥 Cập nhật toàn bộ danh sách trận đấu
    public void updateData(List<Match> newMatches) {
        originalMatches.clear();
        originalMatches.addAll(newMatches);
        filterMatches(""); // Giữ nguyên danh sách nếu không có filter
    }

    // 🔍 Lọc danh sách trận đấu theo tên đội bóng
    public void filterMatches(String query) {
        filteredMatches.clear();
        if (query.isEmpty()) {
            filteredMatches.addAll(originalMatches);
        } else {
            for (Match match : originalMatches) {
                if (match.getHomeTeam().getName().toLowerCase().contains(query.toLowerCase()) ||
                        match.getAwayTeam().getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredMatches.add(match);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvCompetition, tvScore, tvHomeTeam, tvAwayTeam, tvTime, tvStatus;
        ImageView ivHomeTeam, ivAwayTeam;

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
        }
    }
}
