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

    /** 🔥 Save or update match if there are changes */
    public void saveOrUpdateMatch(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .get(Source.CACHE) // 🔥 Try to read from cache first
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Match existingMatch = documentSnapshot.toObject(Match.class);
                        if (existingMatch != null && !isMatchChanged(existingMatch, match)) {
                            Log.d("Firestore", "No changes detected, skipping update for match: " + match.getMatchId());
                            return; // ⏩ Skip if no changes
                        }
                    }
                    updateMatchInFirestore(match); // 🔥 Only write if there are changes
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking match", e);
                    // If cache check fails, update anyway
                    updateMatchInFirestore(match);
                });
    }

    private void updateMatchInFirestore(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .set(match)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Match saved/updated: " + match.getMatchId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving match", e));
    }

    /** 🔍 Check if match has changed */
    private boolean isMatchChanged(Match oldMatch, Match newMatch) {
        return !Objects.equals(oldMatch.getScore(), newMatch.getScore()) ||
                !Objects.equals(oldMatch.getStatus(), newMatch.getStatus()) ||
                !Objects.equals(oldMatch.getHomeTeam().getCrest(), newMatch.getHomeTeam().getCrest()) ||
                !Objects.equals(oldMatch.getAwayTeam().getCrest(), newMatch.getAwayTeam().getCrest());
    }

    /** 🔥 Get all matches for a competition */
    public void getMatchesByCompetition(String competitionName, OnMatchesLoadedListener listener) {
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Match> matches = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Match match = document.toObject(Match.class);
                        if (match != null) {
                            matches.add(match);
                        }
                    }
                    listener.onMatchesLoaded(matches);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting matches", e);
                    listener.onError(e.getMessage());
                });
    }

    /** 🔥 Listen for real-time match updates */
    public void listenForMatchUpdates(String competitionName, OnMatchesUpdatedListener listener) {
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionName)
                .get(Source.CACHE) // 🔥 Get from cache first for quick display
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        List<Match> matches = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Match match = doc.toObject(Match.class);
                            if (match != null) {
                                matches.add(match);
                            }
                        }
                        listener.onMatchesUpdated(matches);
                    }
                    listenForLiveUpdates(competitionName, listener); // Start listening for new data
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Cache fetch failed", e);
                    listenForLiveUpdates(competitionName, listener);
                });
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
                            Match match = change.getDocument().toObject(Match.class);
                            if (match != null) {
                                updatedMatches.add(match);
                            }
                        }
                    }
                    listener.onMatchesUpdated(updatedMatches);
                });
    }

    /** 🔥 Interface callbacks */
    public interface OnMatchesUpdatedListener {
        void onMatchesUpdated(List<Match> matches);
    }

    public interface OnMatchesLoadedListener {
        void onMatchesLoaded(List<Match> matches);
        void onError(String errorMessage);
    }
    public void saveStandings(String tournamentId, List<Standing> standings) {
        List<Map<String, Object>> standingsList = new ArrayList<>();

        for (Standing standing : standings) {
            Map<String, Object> teamData = new HashMap<>();
            teamData.put("teamName", standing.getTeam().getName());
            teamData.put("teamCrest", standing.getTeam().getCrest());
            teamData.put("position", standing.getPosition());
            teamData.put("playedGames", standing.getPlayedGames());
            teamData.put("won", standing.getWon());
            teamData.put("draw", standing.getDraw());
            teamData.put("lost", standing.getLost());
            teamData.put("points", standing.getPoints());
            teamData.put("goalsFor", standing.getGoalsFor());
            teamData.put("goalsAgainst", standing.getGoalsAgainst());
            teamData.put("goalDifference", standing.getGoalDifference());

            standingsList.add(teamData);
        }

        Map<String, Object> standingsData = new HashMap<>();
        standingsData.put("standings", standingsList);

        db.collection("standings").document(tournamentId)
                .set(standingsData)
                .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Standings saved successfully"))
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error saving standings", e));
    }
    public void shouldUpdateStandings(String tournamentId, Runnable onUpdateNeeded) {
        db.collection("standings").document(tournamentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long lastUpdated = documentSnapshot.getLong("lastUpdated");
                        long currentTime = System.currentTimeMillis();
                        if (lastUpdated != null && (currentTime - lastUpdated) < 12 * 60 * 60 * 1000) {
                            Log.d("FIRESTORE", "Standings data is recent, no update needed.");
                            return;
                        }
                    }
                    onUpdateNeeded.run(); // Gọi API fetch nếu cần
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error checking update time", e));
    }

}