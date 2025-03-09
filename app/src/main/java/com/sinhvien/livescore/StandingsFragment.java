package com.sinhvien.livescore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.StandingAdapter;
import com.sinhvien.livescore.Standing;
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

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, leagues);
        spinnerLeague.setAdapter(spinnerAdapter);

        spinnerLeague.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadStandingsFromFirestore(leagues[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private void loadStandingsFromFirestore(String league) {
        db.collection("standings").document(league)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        standingsList.clear();
                        List<Standing> standings = (List<Standing>) documentSnapshot.get("table");
                        if (standings != null) {
                            standingsList.addAll(standings);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("Firestore", "Không có dữ liệu standings!");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Lỗi tải standings", e));
    }
}
