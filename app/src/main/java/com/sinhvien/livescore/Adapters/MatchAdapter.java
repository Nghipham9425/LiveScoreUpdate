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
    private List<Match> matchList;   // Danh sách gốc
    private List<Match> filteredList; // Danh sách đã lọc

    public MatchAdapter(Context context, List<Match> matches) {
        this.context = context;
        this.matchList = new ArrayList<>(matches);
        this.filteredList = new ArrayList<>(matches); // Sao chép danh sách gốc
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
        holder.tvTime.setText(match.getMatchTime());

        // Load hình ảnh logo đội bóng
        Glide.with(context).load(match.getHomeTeam().getCrest()).into(holder.ivHomeTeam);
        Glide.with(context).load(match.getAwayTeam().getCrest()).into(holder.ivAwayTeam);

        // Cập nhật trạng thái trận đấu
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
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Cập nhật danh sách trận đấu mới
    public void updateData(List<Match> newMatches) {
        this.matchList = new ArrayList<>(newMatches);
        this.filteredList = new ArrayList<>(newMatches);
        notifyDataSetChanged();
    }

    // ✅ Thêm chức năng lọc trận đấu
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
