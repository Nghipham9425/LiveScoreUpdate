package com.sinhvien.livescore.FireBase;

import android.util.Log;
import com.google.firebase.firestore.*;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Models.Standing;

import java.util.*;

public class FirebaseHelper {
    private final FirebaseFirestore db;
    private static final String MATCHES_COLLECTION = "matches";
    private static final String STANDINGS_COLLECTION = "standings";

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
    }

    /** 🔥 Lưu trận đấu, chỉ cập nhật nếu có thay đổi */
    public void saveOrUpdateMatch(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .get(Source.CACHE) // 🔥 Ưu tiên đọc từ cache trước
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Match existingMatch = documentSnapshot.toObject(Match.class);
                        if (existingMatch != null && !isMatchChanged(existingMatch, match)) {
                            Log.d("Firestore", "No changes detected, skipping update");
                            return; // ⏩ Không có thay đổi thì không ghi
                        }
                    }
                    updateMatchInFirestore(match); // 🔥 Chỉ ghi nếu có thay đổi
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking match", e));
    }

    private void updateMatchInFirestore(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .set(match)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Match saved/updated!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving match", e));
    }

    /** 🔍 Kiểm tra xem trận đấu có thay đổi không */
    private boolean isMatchChanged(Match oldMatch, Match newMatch) {
        return !Objects.equals(oldMatch.getScore(), newMatch.getScore()) ||
                !Objects.equals(oldMatch.getStatus(), newMatch.getStatus()) ||
                !Objects.equals(oldMatch.getHomeTeam().getCrest(), newMatch.getHomeTeam().getCrest()) ||
                !Objects.equals(oldMatch.getAwayTeam().getCrest(), newMatch.getAwayTeam().getCrest());
    }

    /** 🔥 Lắng nghe cập nhật trận đấu theo thời gian thực */
    public void listenForMatchUpdates(String competitionName, OnMatchesUpdatedListener listener) {
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionName)
                .get(Source.CACHE) // 🔥 Lấy từ cache trước để hiển thị nhanh
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Match> matches = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            matches.add(doc.toObject(Match.class));
                        }
                        listener.onMatchesUpdated(matches);
                    }
                    listenForLiveUpdates(competitionName, listener); // Bắt đầu lắng nghe dữ liệu mới
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Cache fetch failed", e));
    }

    private void listenForLiveUpdates(String competitionName, OnMatchesUpdatedListener listener) {
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionName)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Listen failed", e);
                        return;
                    }
                    if (querySnapshot == null) return;

                    List<Match> updatedMatches = new ArrayList<>();
                    for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED ||
                                change.getType() == DocumentChange.Type.MODIFIED) {
                            updatedMatches.add(change.getDocument().toObject(Match.class));
                        }
                    }
                    listener.onMatchesUpdated(updatedMatches);
                });
    }

    /** 🔥 Lưu bảng xếp hạng */
    public void saveStandings(String competitionId, List<Standing> standings) {
        Map<String, Object> data = new HashMap<>();
        data.put("competitionId", competitionId);
        data.put("timestamp", System.currentTimeMillis());
        data.put("standings", standings);

        db.collection(STANDINGS_COLLECTION).document(competitionId)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Standings saved!"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving standings", e));
    }

    /** 🔥 Interface callback */
    public interface OnMatchesUpdatedListener {
        void onMatchesUpdated(List<Match> matches);
    }
}
