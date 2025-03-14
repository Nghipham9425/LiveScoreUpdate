package com.sinhvien.livescore.Fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.sinhvien.livescore.Adapters.MatchAdapter;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.R;

import java.util.*;

public class FavoriteFragment extends Fragment {
    private RecyclerView recyclerView;
    private MatchAdapter matchAdapter;
    private List<Match> favoriteMatches = new ArrayList<>();
    private TextView tvNoFavorites;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        tvNoFavorites = view.findViewById(R.id.tvNoFavorites);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Lấy uid, nếu chưa đăng nhập thì uid = ""
        String uid = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        matchAdapter = new MatchAdapter(getContext(), favoriteMatches, uid);
        recyclerView.setAdapter(matchAdapter);

        if (!uid.isEmpty()) {
            loadFavoriteMatches(uid);
        } else {
            tvNoFavorites.setVisibility(View.VISIBLE);
            tvNoFavorites.setText("Bạn chưa đăng nhập!");
        }

        return view;
    }

    private void loadFavoriteMatches(String uid) {
        FirebaseFirestore.getInstance().collection("Users").document(uid) // Sửa thành "Users"
                .collection("favorites")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        return;
                    }
                    favoriteMatches.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Match match = doc.toObject(Match.class);
                        favoriteMatches.add(match);
                    }
                    matchAdapter.updateData(favoriteMatches);

                    // Kiểm tra nếu không có trận yêu thích
                    if (favoriteMatches.isEmpty()) {
                        tvNoFavorites.setVisibility(View.VISIBLE);
                        tvNoFavorites.setText("Chưa có trận nào yêu thích!");
                    } else {
                        tvNoFavorites.setVisibility(View.GONE);
                    }
                });
    }
}
