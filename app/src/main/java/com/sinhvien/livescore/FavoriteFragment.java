package com.sinhvien.livescore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.Color;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.MatchAdapter;
import com.sinhvien.livescore.Match;
import com.sinhvien.livescore.Team;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;


public class FavoriteFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false); // Đầu tiên, inflate layout

        View layout = view.findViewById(R.id.rootLayout); // Lấy layout gốc

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int savedColor = sharedPreferences.getInt("backgroundColor", Color.WHITE);
        layout.setBackgroundColor(savedColor);

        return view;
    }
}


