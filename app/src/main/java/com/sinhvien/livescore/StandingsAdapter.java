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

public class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.ViewHolder> {
    private List<TeamStanding> standingsList;

    // 🔥 Constructor nhận danh sách standings
    public StandingsAdapter(List<TeamStanding> standingsList) {
        this.standingsList = standingsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamStanding team = standingsList.get(position);
        holder.position.setText(String.valueOf(team.getPosition()));
        holder.teamName.setText(team.getTeamName());
        holder.playedGames.setText("PL: " + team.getPlayedGames());
        holder.points.setText("PTS: " + team.getPoints());

        // Load logo bằng Glide
        Glide.with(holder.itemView.getContext())
                .load(team.getTeamLogo())
                .into(holder.teamLogo);
    }

    @Override
    public int getItemCount() {
        return standingsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView position, teamName, playedGames, points;
        ImageView teamLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.textPosition);
            teamName = itemView.findViewById(R.id.textTeamName);
            playedGames = itemView.findViewById(R.id.textPlayedGames);
            points = itemView.findViewById(R.id.textPoints);
            teamLogo = itemView.findViewById(R.id.imageTeamLogo);
        }
    }
}
