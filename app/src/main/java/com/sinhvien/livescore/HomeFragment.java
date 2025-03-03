package com.sinhvien.livescore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;

public class HomeFragment extends Fragment implements MainActivity.MatchDataListener {
    private RecyclerView recyclerView;
    private MatchAdapter adapter;
    private Spinner spinnerLeague;
    private List<Match> allMatches = new ArrayList<>();
    private List<Competition> competitions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        View layout = view.findViewById(R.id.rootLayout);

        // Lấy màu nền từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int savedColor = sharedPreferences.getInt("backgroundColor", Color.WHITE);
        layout.setBackgroundColor(savedColor);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MatchAdapter(allMatches);
        recyclerView.setAdapter(adapter);

        spinnerLeague = view.findViewById(R.id.spinnerLeague);
        setupLeagueSpinner();

        ((MainActivity) requireActivity()).setMatchDataListener(this);
    }

    private void setupLeagueSpinner() {
        List<Competition> manualCompetitions = new ArrayList<>();
        manualCompetitions.add(new Competition("PL", "Premier League"));
        manualCompetitions.add(new Competition("LL", "La Liga"));
        manualCompetitions.add(new Competition("SA", "Serie A"));
        manualCompetitions.add(new Competition("BL", "Bundesliga"));
        manualCompetitions.add(new Competition("L1", "Ligue 1"));
        manualCompetitions.add(new Competition("UCL", "UEFA Champions League"));

        competitions = manualCompetitions;

        List<String> leagueNames = new ArrayList<>();
        leagueNames.add("All Competitions");
        for (Competition competition : competitions) {
            leagueNames.add(competition.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, leagueNames);
        spinnerLeague.setAdapter(adapter);

        spinnerLeague.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLeague = position == 0 ? "All Competitions" : competitions.get(position - 1).getId();
                Log.d("HomeFragment", "Giải đấu đã chọn: " + selectedLeague);
                filterMatchesByLeague(selectedLeague);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filterMatchesByLeague("All Competitions");
            }
        });
    }

    @Override
    public void onMatchDataFetched(List<Match> matches) {
        allMatches = matches;
        filterMatchesByLeague(spinnerLeague.getSelectedItemPosition() == 0 ? "All Competitions" : competitions.get(spinnerLeague.getSelectedItemPosition() - 1).getId());
    }

    private void filterMatchesByLeague(String leagueId) {
        List<Match> filteredMatches = new ArrayList<>();

        for (Match match : allMatches) {
            Log.d("FILTER", "Match: " + match.getCompetition());
            if (leagueId.equals("All Competitions") || match.getCompetition().equalsIgnoreCase(leagueId)) {
                filteredMatches.add(match);
            }
        }

        Log.d("FILTER", "Filtered Matches Count: " + filteredMatches.size());

        adapter.setMatches(filteredMatches);
        adapter.notifyDataSetChanged();
    }
}
