package com.sinhvien.livescore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StandingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private StandingsAdapter adapter;
    private List<TeamStanding> standingsList = new ArrayList<>();
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standings, container, false);

        // Ánh xạ RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewStandings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter và set vào RecyclerView
        adapter = new StandingsAdapter(standingsList);
        recyclerView.setAdapter(adapter);

        // Tham chiếu đến Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("standings");

        // Load dữ liệu từ Firebase
        saveSampleStandingsToFirebase();

        return view;
    }

    private void loadStandingsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                standingsList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    TeamStanding team = data.getValue(TeamStanding.class);
                    if (team != null) {
                        standingsList.add(team);
                    } else {
                        Log.e("Firebase", "DataSnapshot không đúng định dạng: " + data);
                    }
                }

                // Cập nhật giao diện trên UI thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load standings", error.toException());
            }
        });
    }
    private void saveSampleStandingsToFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("standings");

        List<TeamStanding> sampleData = new ArrayList<>();
        sampleData.add(new TeamStanding(1, "Manchester City", 25, 58, 35, "https://upload.wikimedia.org/wikipedia/en/e/eb/Manchester_City_FC_badge.svg"));
        sampleData.add(new TeamStanding(2, "Liverpool", 25, 56, 32, "https://upload.wikimedia.org/wikipedia/en/0/0c/Liverpool_FC.svg"));
        sampleData.add(new TeamStanding(3, "Arsenal", 25, 54, 30, "https://upload.wikimedia.org/wikipedia/en/5/53/Arsenal_FC.svg"));

        for (TeamStanding team : sampleData) {
            databaseReference.push().setValue(team);
        }
    }

}
