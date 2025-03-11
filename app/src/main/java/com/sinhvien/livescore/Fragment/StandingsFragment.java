package com.sinhvien.livescore.Fragment;

import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.firestore.*;
import com.sinhvien.livescore.Adapters.StandingAdapter;
import com.sinhvien.livescore.Models.Standing;
import com.sinhvien.livescore.R;

import java.util.ArrayList;
import java.util.List;

public class StandingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private StandingAdapter adapter;
    private List<Standing> standingsList = new ArrayList<>();
    private FirebaseFirestore db;
    private Spinner spinnerLeague;
    private String[] leagues = {"Premier League", "Serie A", "Bundesliga", "Primera Division", "Ligue 1", "UEFA Champions League"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standings, container, false);

        spinnerLeague = view.findViewById(R.id.spinnerLeague);
        recyclerView = view.findViewById(R.id.recyclerViewStandings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StandingAdapter(standingsList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Setup Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, leagues);
        spinnerLeague.setAdapter(spinnerAdapter);

        spinnerLeague.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStandingsFromFirestore(leagues[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no selection
            }
        });

        return view;
    }

    private void loadStandingsFromFirestore(String league) {
        db.collection("standings")
                .document(league)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        standingsList.clear();
                        List<Standing> standings = (List<Standing>) documentSnapshot.get("table");
                        if (standings != null) {
                            standingsList.addAll(standings);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No standings data available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Firestore", "No data found for " + league);
                        Toast.makeText(getContext(), "No standings data for " + league, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading standings", e);
                    Toast.makeText(getContext(), "Failed to load standings", Toast.LENGTH_SHORT).show();
                });
    }
}
