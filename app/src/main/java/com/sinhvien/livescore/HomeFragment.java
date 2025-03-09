package com.sinhvien.livescore;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import java.util.*;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MatchAdapter matchAdapter;
    private FirebaseHelper firebaseHelper;
    private Spinner spinnerCompetition;
    private List<Match> matchList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        spinnerCompetition = view.findViewById(R.id.spinnerLeague);
        firebaseHelper = new FirebaseHelper();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        matchAdapter = new MatchAdapter(getContext(), matchList);
        recyclerView.setAdapter(matchAdapter);

        setupSpinner();
        return view;
    }

    private void setupSpinner() {
        String[] leagues = {"Premier League", "Primera Division", "Serie A", "Bundesliga", "Ligue 1", "UEFA Champions League"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, leagues);
        spinnerCompetition.setAdapter(adapter);

        spinnerCompetition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLeague = leagues[position];
                loadMatches(selectedLeague);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // 🔥 Sử dụng snapshot listener để tải dữ liệu theo thời gian thực
    private void loadMatches(String competitionName) {
        firebaseHelper.listenForMatchUpdates(competitionName, updatedMatches -> {
            matchList.clear();
            matchList.addAll(updatedMatches);
            matchAdapter.notifyDataSetChanged();
        });
    }
}
