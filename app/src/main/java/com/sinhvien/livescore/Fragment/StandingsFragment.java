package com.sinhvien.livescore.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinhvien.livescore.Adapters.StandingAdapter;
import com.sinhvien.livescore.Models.Standing;
import com.sinhvien.livescore.Models.Team;
import com.sinhvien.livescore.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StandingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private StandingAdapter adapter;
    private List<Standing> standingsList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner leagueSpinner;

    // Map ID của giải đấu với tên hiển thị trên Spinner
    private final Map<String, String> leagueMap = new HashMap<String, String>() {{
        put("PL", "Premier League");
        put("BL1", "Bundesliga");
        put("SA", "Serie A");
        put("PD", "Primera División"); // La Liga
        put("FL1", "Ligue 1");
        put("CL", "Champions League");
    }};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standings, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewStandings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        leagueSpinner = view.findViewById(R.id.leagueSpinner);

        setupRecyclerView();
        setupLeagueSpinner();
        setupSwipeRefresh();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new StandingAdapter(standingsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupLeagueSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(leagueMap.values())
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leagueSpinner.setAdapter(spinnerAdapter);

        leagueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String leagueName = parent.getItemAtPosition(position).toString();
                String leagueId = getLeagueIdByName(leagueName);
                loadStandingsData(leagueId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private String getLeagueIdByName(String leagueName) {
        for (Map.Entry<String, String> entry : leagueMap.entrySet()) {
            if (entry.getValue().equals(leagueName)) {
                return entry.getKey();
            }
        }
        return "PL"; // Mặc định là Premier League nếu không tìm thấy
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String leagueName = leagueSpinner.getSelectedItem().toString();
            String leagueId = getLeagueIdByName(leagueName);
            loadStandingsData(leagueId);
        });
    }

    private void loadStandingsData(String competitionId) {
        swipeRefreshLayout.setRefreshing(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("standings")
                .document(competitionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        processStandingsData(documentSnapshot);
                    } else {
                        Toast.makeText(getContext(), "Không có dữ liệu bảng xếp hạng", Toast.LENGTH_SHORT).show();
                        standingsList.clear();
                        adapter.notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    @SuppressWarnings("unchecked")
    private void processStandingsData(DocumentSnapshot documentSnapshot) {
        List<HashMap<String, Object>> standingsData = (List<HashMap<String, Object>>) documentSnapshot.get("standings");
        standingsList.clear();

        if (standingsData != null) {
            for (HashMap<String, Object> standingMap : standingsData) {
                Standing standing = convertMapToStanding(standingMap);
                standingsList.add(standing);
            }
            standingsList.sort(Comparator.comparingInt(Standing::getPosition));
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("unchecked")
    private Standing convertMapToStanding(HashMap<String, Object> standingMap) {
        int position = ((Long) standingMap.get("position")).intValue();
        int playedGames = ((Long) standingMap.get("playedGames")).intValue();
        int won = ((Long) standingMap.get("won")).intValue();
        int draw = ((Long) standingMap.get("draw")).intValue();
        int lost = ((Long) standingMap.get("lost")).intValue();
        int points = ((Long) standingMap.get("points")).intValue();
        int goalsFor = ((Long) standingMap.get("goalsFor")).intValue();
        int goalsAgainst = ((Long) standingMap.get("goalsAgainst")).intValue();
        int goalDifference = ((Long) standingMap.get("goalDifference")).intValue();
        String form = (String) standingMap.get("form");

        String teamName = (String) standingMap.get("teamName");
        String teamShortName = (String) standingMap.get("teamShortName");
        String teamTla = (String) standingMap.get("teamTla");
        String teamCrest = (String) standingMap.get("teamCrest");

        Team team = new Team(teamName, teamShortName, teamTla, teamCrest);
        return new Standing(position, team, playedGames, form, won, draw, lost, points, goalsFor, goalsAgainst, goalDifference);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStandingsData("PL");
    }
}