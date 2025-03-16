package com.sinhvien.livescore.FireBase;

import android.util.Log;
import com.google.firebase.firestore.*;
import com.google.firebase.Timestamp; // Add this import
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Models.Standing;
import com.google.firebase.firestore.FieldValue;

import java.util.*;

public class FirebaseHelper {
    private final FirebaseFirestore db;
    private static final String MATCHES_COLLECTION = "Matches";
    private static final String STANDINGS_COLLECTION = "standings";

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
    }

    /**
     * 🔥 Save or update match if there are changes
     */
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

    /**
     * 🔍 Check if match has changed
     */
    private boolean isMatchChanged(Match oldMatch, Match newMatch) {
        return !Objects.equals(oldMatch.getScore(), newMatch.getScore()) ||
                !Objects.equals(oldMatch.getStatus(), newMatch.getStatus()) ||
                !Objects.equals(oldMatch.getHomeTeam().getCrest(), newMatch.getHomeTeam().getCrest()) ||
                !Objects.equals(oldMatch.getAwayTeam().getCrest(), newMatch.getAwayTeam().getCrest());
    }

    /**
     * 🔥 Get all matches for a competition
     */
    public void getMatchesByCompetition(String competitionName, OnMatchesLoadedListener listener) {
        String competitionCode = convertCompetitionNameToCode(competitionName);

        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionCode)
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

    /**
     * 🔥 Listen for real-time match updates
     */
    public void listenForMatchUpdates(String competitionName, OnMatchesUpdatedListener listener) {
        // Chuyển đổi tên giải đấu thành mã code
        String competitionCode = convertCompetitionNameToCode(competitionName);

        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionCode)
                .get(Source.CACHE)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Match> matches = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Match match = document.toObject(Match.class);
                        if (match != null) {
                            matches.add(match);
                        }
                    }
                    listener.onMatchesUpdated(matches);

                    // Also setup live updates after getting cached data
                    listenForLiveUpdates(competitionCode, listener);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting cached matches", e);
                    // On cache failure, directly listen for live updates
                    listenForLiveUpdates(competitionCode, listener);
                });
    }

    private String convertCompetitionNameToCode(String competitionName) {
        switch (competitionName) {
            case "Premier League":
                return "PL";
            case "Primera Division":
                return "PD";
            case "Serie A":
                return "SA";
            case "Bundesliga":
                return "BL1";
            case "Ligue 1":
                return "FL1";
            case "UEFA Champions League":
                return "CL";
            default:
                return competitionName;
        }
    }

    private void listenForLiveUpdates(String competitionCode, OnMatchesUpdatedListener listener) {
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", competitionCode)
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

    /**
     * 🔥 Interface callbacks
     */
    public interface OnMatchesUpdatedListener {
        void onMatchesUpdated(List<Match> matches);
    }

    public interface OnMatchesLoadedListener {
        void onMatchesLoaded(List<Match> matches);

        void onError(String errorMessage);
    }

    public void shouldUpdateMatches(String tournament, Runnable onShouldUpdate) {
        String tournamentCode = convertCompetitionNameToCode(tournament);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(MATCHES_COLLECTION).document(tournamentCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If document doesn't exist or is older than 24 hours, update it
                    if (!documentSnapshot.exists() || isDataStale(documentSnapshot)) {
                        onShouldUpdate.run();
                    }
                })
                .addOnFailureListener(e -> onShouldUpdate.run());
    }

    private boolean isDataStale(DocumentSnapshot documentSnapshot) {
        // Check if document has lastUpdated field
        Date lastUpdated = documentSnapshot.getDate("lastUpdated");
        if (lastUpdated == null) {
            return true; // Consider stale if no timestamp
        }

        // Check if data is older than 24 hours
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24);
        return lastUpdated.before(calendar.getTime());
    }

    public void saveMatches(String tournament, List<Match> matches) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String tournamentCode = convertCompetitionNameToCode(tournament);

        // Tạo document cho metadata của giải đấu
        Map<String, Object> tournamentData = new HashMap<>();
        tournamentData.put("lastUpdated", FieldValue.serverTimestamp()); // Server timestamp instead of new Date()
        tournamentData.put("matchCount", matches.size());

        db.collection("tournaments").document(tournamentCode)
                .set(tournamentData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Lưu metadata giải đấu thành công"))
                .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi lưu metadata", e));

        // Sử dụng nhiều transaction nhỏ thay vì một batch lớn để tránh vượt quá giới hạn
        int batchSize = 20; // Firestore có giới hạn 500 operations/batch

        for (int i = 0; i < matches.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, matches.size());
            WriteBatch batch = db.batch();

            for (int j = i; j < endIndex; j++) {
                Match match = matches.get(j);
                match.setCompetition(tournamentCode);

                DocumentReference matchRef = db.collection(MATCHES_COLLECTION).document(match.getMatchId());
                batch.set(matchRef, match);
            }

            int finalI = i;
            batch.commit()
                    .addOnSuccessListener(aVoid ->
                            Log.d("Firebase", "Batch " + (finalI / batchSize + 1) + " lưu thành công"))
                    .addOnFailureListener(e ->
                            Log.e("Firebase", "Lỗi khi lưu batch " + (finalI / batchSize + 1), e));
        }
    }

    public void saveStandings(String tournamentId, List<Standing> standings) {
        String tournamentCode = convertCompetitionNameToCode(tournamentId);
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
        // Use server timestamp instead of Date
        standingsData.put("lastUpdated", FieldValue.serverTimestamp());

        db.collection(STANDINGS_COLLECTION).document(tournamentCode)
                .set(standingsData)
                .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Standings saved successfully"))
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error saving standings", e));
    }

    public void shouldUpdateStandings(String tournamentId, Runnable onUpdateNeeded) {
        String tournamentCode = convertCompetitionNameToCode(tournamentId);

        db.collection(STANDINGS_COLLECTION).document(tournamentCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Handle different possible formats of lastUpdated
                        Object lastUpdatedObj = documentSnapshot.get("lastUpdated");
                        boolean isStale = true;

                        if (lastUpdatedObj != null) {
                            Date lastUpdated = null;

                            // Check if it's a Timestamp
                            if (lastUpdatedObj instanceof Timestamp) {
                                lastUpdated = ((Timestamp) lastUpdatedObj).toDate();
                            }
                            // Check if it's already a Date (shouldn't happen, but just in case)
                            else if (lastUpdatedObj instanceof Date) {
                                lastUpdated = (Date) lastUpdatedObj;
                            }
                            // Handle String or other formats if needed

                            if (lastUpdated != null) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.HOUR, -12);
                                if (lastUpdated.after(calendar.getTime())) {
                                    // Data is less than 12 hours old
                                    Log.d("FIRESTORE", "Standings data is recent, no update needed.");
                                    isStale = false;
                                }
                            }
                        }

                        if (isStale) {
                            onUpdateNeeded.run();
                        }
                    } else {
                        onUpdateNeeded.run(); // Document doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error checking update time", e);
                    onUpdateNeeded.run(); // Call API on error to be safe
                });
    }
}