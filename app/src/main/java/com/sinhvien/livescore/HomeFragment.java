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
    private List<Competition> competitions = new ArrayList<>(); // Để lưu danh sách giải đấu

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        View layout = view.findViewById(R.id.rootLayout);

        // Lấy màu nền từ SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int savedColor = sharedPreferences.getInt("backgroundColor", Color.WHITE);
        layout.setBackgroundColor(savedColor); // Áp dụng màu nền cho layout

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo Adapter cho RecyclerView
        adapter = new MatchAdapter(allMatches);
        recyclerView.setAdapter(adapter);

        // Khởi tạo Spinner để chọn giải đấu
        spinnerLeague = view.findViewById(R.id.spinnerLeague);
        setupLeagueSpinner();

        // Đăng ký lắng nghe dữ liệu từ MainActivity
        ((MainActivity) requireActivity()).setMatchDataListener(this);
    }

    // Hàm để thiết lập Spinner với các giải đấu
    private void setupLeagueSpinner() {
        // Danh sách giải đấu thủ công
        List<Competition> manualCompetitions = new ArrayList<>();
        manualCompetitions.add(new Competition("PL", "Premier League"));
        manualCompetitions.add(new Competition("LL", "La Liga"));
        manualCompetitions.add(new Competition("SA", "Serie A"));
        manualCompetitions.add(new Competition("BL", "Bundesliga"));
        manualCompetitions.add(new Competition("L1", "Ligue 1"));
        manualCompetitions.add(new Competition("UCL", "UEFA Champions League"));

        // Cập nhật danh sách giải đấu từ danh sách thủ công
        competitions = manualCompetitions;

        // Cập nhật Spinner với các giải đấu thủ công
        List<String> leagueNames = new ArrayList<>();
        leagueNames.add("All Competitions"); // Tạo mục "Tất cả giải đấu"
        for (Competition competition : competitions) {
            leagueNames.add(competition.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, leagueNames);
        spinnerLeague.setAdapter(adapter);

        // Lắng nghe sự kiện chọn giải đấu từ Spinner
        spinnerLeague.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Kiểm tra và log giải đấu đang chọn
                String selectedLeague = position == 0 ? "All Competitions" : competitions.get(position - 1).getId();
                Log.d("HomeFragment", "Giải đấu đã chọn: " + selectedLeague);
                filterMatchesByLeague(selectedLeague);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nếu không chọn gì, hiển thị tất cả trận đấu
                filterMatchesByLeague("All Competitions");
            }
        });
    }

    // Hàm xử lý dữ liệu trận đấu đã được fetch
    @Override
    public void onMatchDataFetched(List<Match> matches) {
        allMatches = matches;
        adapter.setMatches(allMatches); // Cập nhật adapter với dữ liệu mới
    }

    // Hàm lọc danh sách trận đấu theo giải đấu
    private void filterMatchesByLeague(String leagueId) {
        List<Match> filteredMatches = new ArrayList<>();
        for (Match match : allMatches) {
            // Kiểm tra nếu giải đấu trong Match có khớp với giải đấu đang chọn
            if (leagueId.equals("All Competitions") || match.getCompetition().equals(leagueId)) {
                filteredMatches.add(match);
            }
        }
        adapter.setMatches(filteredMatches); // Cập nhật adapter với danh sách đã lọc
    }
}
