package com.sinhvien.livescore.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sinhvien.livescore.Models.Standing;
import com.sinhvien.livescore.R;

import java.util.List;

public class StandingAdapter extends RecyclerView.Adapter<StandingAdapter.StandingViewHolder> {
    private List<Standing> standingList;

    public StandingAdapter(List<Standing> standingList) {
        this.standingList = standingList;
    }

    @NonNull
    @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standing, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        Standing standing = standingList.get(position);
        holder.tvRank.setText(String.valueOf(standing.getPosition()));
        holder.tvTeamName.setText(standing.getTeam().getName());
        holder.tvWins.setText(String.valueOf(standing.getWon()));
        holder.tvDraws.setText(String.valueOf(standing.getDraw()));
        holder.tvLosses.setText(String.valueOf(standing.getLost()));
        holder.tvPoints.setText(String.valueOf(standing.getPoints()));

        Glide.with(holder.itemView.getContext())
                .load(standing.getTeam().getCrest())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imgTeamLogo);
    }

    @Override
    public int getItemCount() {
        return standingList.size();
    }

    public void updateData(List<Standing> newStandings) {
        standingList.clear();
        standingList.addAll(newStandings);
        notifyDataSetChanged();
    }

    public static class StandingViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvTeamName, tvWins, tvDraws, tvLosses, tvPoints;
        ImageView imgTeamLogo;

        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvWins = itemView.findViewById(R.id.tvWins);
            tvDraws = itemView.findViewById(R.id.tvDraws);
            tvLosses = itemView.findViewById(R.id.tvLosses);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            imgTeamLogo = itemView.findViewById(R.id.imgTeamLogo);
        }
    }
}
