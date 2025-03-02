package com.sinhvien.livescore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private List<Match> matchList; // Đảm bảo sử dụng List<Match>

    public MatchAdapter(List<Match> matchList) {
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchList.get(position); // Dùng Match thay vì MatchItem

        holder.tvHomeTeam.setText(match.getHomeTeam().getName());
        holder.tvAwayTeam.setText(match.getAwayTeam().getName());
        holder.tvCompetition.setText(match.getCompetition());
        holder.tvScore.setText(match.getScore());
        holder.tvTime.setText(match.getTime());

        // Hiển thị logo đội bóng
        Glide.with(holder.itemView.getContext()).load(match.getHomeTeam().getCrest()).into(holder.ivHomeTeam);
        Glide.with(holder.itemView.getContext()).load(match.getAwayTeam().getCrest()).into(holder.ivAwayTeam);
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvHomeTeam, tvAwayTeam, tvCompetition, tvScore, tvTime;
        ImageView ivHomeTeam, ivAwayTeam;

        public MatchViewHolder(View itemView) {
            super(itemView);
            tvHomeTeam = itemView.findViewById(R.id.tvHomeTeam);
            tvAwayTeam = itemView.findViewById(R.id.tvAwayTeam);
            tvCompetition = itemView.findViewById(R.id.tvCompetition);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivHomeTeam = itemView.findViewById(R.id.ivHomeTeam);
            ivAwayTeam = itemView.findViewById(R.id.ivAwayTeam);
        }
    }

    // Phương thức setMatches để cập nhật danh sách dữ liệu
    public void setMatches(List<Match> matches) {
        this.matchList = matches;
        notifyDataSetChanged();
    }
}
