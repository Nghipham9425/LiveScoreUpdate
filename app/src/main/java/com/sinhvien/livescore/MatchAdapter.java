package com.sinhvien.livescore;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.*;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final Context context;
    private final List<Match> matches;

    public MatchAdapter(Context context, List<Match> matches) {
        this.context = context;
        this.matches = matches;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);

        // Set dữ liệu vào UI
        holder.tvCompetition.setText(match.getCompetition());
        holder.tvScore.setText(match.getScore());
        holder.tvHomeTeam.setText(match.getHomeTeam().getName());
        holder.tvAwayTeam.setText(match.getAwayTeam().getName());
        holder.tvTime.setText(match.getMatchTime());
        holder.tvStatus.setText(match.getStatus()); // Gán trạng thái trận đấu

        // Load ảnh đội bóng bằng Glide
        Glide.with(context).load(match.getHomeTeam().getCrestUrl()).into(holder.ivHomeTeam);
        Glide.with(context).load(match.getAwayTeam().getCrestUrl()).into(holder.ivAwayTeam);

        // 🔥 Đổi màu nền badge dựa theo trạng thái trận đấu
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
        return matches.size();
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
