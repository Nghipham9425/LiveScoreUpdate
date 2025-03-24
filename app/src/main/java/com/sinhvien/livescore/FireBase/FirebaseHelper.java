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

    public void saveOrUpdateMatch(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .get(Source.CACHE)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Match existingMatch = documentSnapshot.toObject(Match.class);
                        if (existingMatch != null && !isMatchChanged(existingMatch, match)) {
                            Log.d("Firestore", "No changes detected, skipping update for match: " + match.getMatchId());
                            return;
                        }
                    }
                    updateMatchInFirestore(match);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error checking match", e);
                    updateMatchInFirestore(match);
                });
    }

    private void updateMatchInFirestore(Match match) {
        db.collection(MATCHES_COLLECTION).document(match.getMatchId())
                .set(match)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Match saved/updated: " + match.getMatchId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving match", e));
    }

    private boolean isMatchChanged(Match oldMatch, Match newMatch) {
        return !Objects.equals(oldMatch.getScore(), newMatch.getScore()) ||
                !Objects.equals(oldMatch.getStatus(), newMatch.getStatus()) ||
                !Objects.equals(oldMatch.getHomeTeam().getCrest(), newMatch.getHomeTeam().getCrest()) ||
                !Objects.equals(oldMatch.getAwayTeam().getCrest(), newMatch.getAwayTeam().getCrest()) ||
                !Objects.equals(oldMatch.getHomeTeam().getShortName(), newMatch.getHomeTeam().getShortName()) ||
                !Objects.equals(oldMatch.getAwayTeam().getShortName(), newMatch.getAwayTeam().getShortName()) ||
                !Objects.equals(oldMatch.getHomeTeam().getTla(), newMatch.getHomeTeam().getTla()) ||
                !Objects.equals(oldMatch.getAwayTeam().getTla(), newMatch.getAwayTeam().getTla());
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
        String tournamentCode = convertCompetitionNameToCode(tournament);

        // Get existing match IDs to determine which ones need to be updated
        db.collection(MATCHES_COLLECTION)
                .whereEqualTo("competition", tournamentCode)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Create a map of existing matches for quick lookup
                    Map<String, Match> existingMatches = new HashMap<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Match match = doc.toObject(Match.class);
                        if (match != null) {
                            existingMatches.put(match.getMatchId(), match);
                        }
                    }

                    // Track new and updated matches
                    List<Match> matchesToSave = new ArrayList<>();

                    for (Match newMatch : matches) {
                        Match existingMatch = existingMatches.get(newMatch.getMatchId());
                        newMatch.setCompetition(tournamentCode);

                        // Add to save list if match is new or status/score has changed
                        if (existingMatch == null ||
                                !existingMatch.getStatus().equals(newMatch.getStatus()) ||
                                !Objects.equals(existingMatch.getScore(), newMatch.getScore())) {
                            matchesToSave.add(newMatch);
                        }
                    }

                    // Only update metadata if we have matches to save
                    if (!matchesToSave.isEmpty()) {
                        saveMatchBatches(matchesToSave, tournamentCode);
                    } else {
                        Log.d("Firebase", "No matches need updating for " + tournamentCode);
                    }
                })
                .addOnFailureListener(e -> {
                    // In case of failure, save all matches to be safe
                    for (Match match : matches) {
                        match.setCompetition(tournamentCode);
                    }
                    saveMatchBatches(matches, tournamentCode);
                });
    }

    private void saveMatchBatches(List<Match> matches, String tournamentCode) {
        // Update tournament metadata
        Map<String, Object> tournamentData = new HashMap<>();
        tournamentData.put("lastUpdated", FieldValue.serverTimestamp());
        tournamentData.put("matchCount", matches.size());

        db.collection("tournaments").document(tournamentCode)
                .set(tournamentData)
                .addOnFailureListener(e -> Log.e("Firebase", "Error saving tournament metadata", e));

        // Save matches in batches
        int batchSize = 20;
        for (int i = 0; i < matches.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, matches.size());
            WriteBatch batch = db.batch();

            for (int j = i; j < endIndex; j++) {
                Match match = matches.get(j);
                DocumentReference matchRef = db.collection(MATCHES_COLLECTION).document(match.getMatchId());
                batch.set(matchRef, match);
            }

            int finalI = i;
            batch.commit()
                    .addOnSuccessListener(aVoid ->
                            Log.d("Firebase", "Batch " + (finalI / batchSize + 1) +
                                    " saved with " + Math.min(batchSize, matches.size() - finalI) + " matches"))
                    .addOnFailureListener(e ->
                            Log.e("Firebase", "Error saving batch " + (finalI / batchSize + 1), e));
        }
    }

    public void saveStandings(String tournamentId, List<Standing> standings) {
        String tournamentCode = convertCompetitionNameToCode(tournamentId);

        // Get current standings to check if update is needed
        db.collection(STANDINGS_COLLECTION).document(tournamentCode)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean needsUpdate = true;

                    if (doc.exists()) {
                        List<Map<String, Object>> currentStandings = (List<Map<String, Object>>) doc.get("standings");
                        if (currentStandings != null && currentStandings.size() == standings.size()) {
                            needsUpdate = hasStandingsChanged(currentStandings, standings);
                        }
                    }

                    if (needsUpdate) {
                        Log.d("STANDINGS_DEBUG", "Updating standings for " + tournamentCode);
                        writeStandings(tournamentCode, standings);
                    } else {
                        Log.d("STANDINGS_DEBUG", "No changes in standings for " + tournamentCode + ", skipping update");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("STANDINGS_DEBUG", "Error checking standings, forcing update", e);
                    writeStandings(tournamentCode, standings);
                });
    }

    private boolean hasStandingsChanged(List<Map<String, Object>> currentStandings, List<Standing> newStandings) {
        for (int i = 0; i < newStandings.size(); i++) {
            Standing newStanding = newStandings.get(i);
            Map<String, Object> currentStanding = currentStandings.get(i);

            // Check if key values have changed
            if (!Objects.equals(currentStanding.get("position"), newStanding.getPosition()) ||
                    !Objects.equals(currentStanding.get("points"), newStanding.getPoints()) ||
                    !Objects.equals(currentStanding.get("playedGames"), newStanding.getPlayedGames()) ||
                    !Objects.equals(currentStanding.get("won"), newStanding.getWon()) ||
                    !Objects.equals(currentStanding.get("draw"), newStanding.getDraw()) ||
                    !Objects.equals(currentStanding.get("lost"), newStanding.getLost())) {
                return true;
            }
        }
        return false;
    }

    private void writeStandings(String tournamentCode, List<Standing> standings) {
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
        standingsData.put("lastUpdated", FieldValue.serverTimestamp());

        db.collection(STANDINGS_COLLECTION).document(tournamentCode)
                .set(standingsData)
                .addOnSuccessListener(aVoid ->
                        Log.d("STANDINGS_DEBUG", "Standings saved for " + tournamentCode))
                .addOnFailureListener(e ->
                        Log.e("STANDINGS_DEBUG", "Error saving standings for " + tournamentCode, e));
    }

    public void shouldUpdateStandings(String tournamentId, Runnable onUpdateNeeded) {
        String tournamentCode = convertCompetitionNameToCode(tournamentId);
        Log.d("STANDINGS_DEBUG", "Checking if update needed for: " + tournamentCode);

        db.collection(STANDINGS_COLLECTION).document(tournamentCode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Log.d("STANDINGS_DEBUG", "No standings document exists for: " + tournamentCode + ", updating...");
                        onUpdateNeeded.run();
                        return;
                    }

                    // Handle different possible formats of lastUpdated
                    Object lastUpdatedObj = documentSnapshot.get("lastUpdated");
                    Log.d("STANDINGS_DEBUG", "Last updated object: " + (lastUpdatedObj != null ? lastUpdatedObj.getClass().getName() : "null"));

                    if (lastUpdatedObj == null) {
                        Log.d("STANDINGS_DEBUG", "No lastUpdated timestamp, forcing update for: " + tournamentCode);
                        onUpdateNeeded.run();
                        return;
                    }

                    Date lastUpdated = null;
                    if (lastUpdatedObj instanceof Timestamp) {
                        lastUpdated = ((Timestamp) lastUpdatedObj).toDate();
                    } else if (lastUpdatedObj instanceof Date) {
                        lastUpdated = (Date) lastUpdatedObj;
                    }

                    if (lastUpdated == null) {
                        Log.d("STANDINGS_DEBUG", "Couldn't parse lastUpdated timestamp, forcing update for: " + tournamentCode);
                        onUpdateNeeded.run();
                        return;
                    }

                    // Calculate if data is stale (older than 2 hours for testing)
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, -2); // Reduced from 12 to 2 hours for testing
                    boolean isStale = lastUpdated.before(calendar.getTime());

                    Log.d("STANDINGS_DEBUG", "Standings for " + tournamentCode +
                            " last updated at: " + lastUpdated +
                            ", isStale: " + isStale);

                    if (isStale) {
                        Log.d("STANDINGS_DEBUG", "Standings data is stale, updating for: " + tournamentCode);
                        onUpdateNeeded.run();
                    } else {
                        Log.d("STANDINGS_DEBUG", "Standings data is recent, no update needed for: " + tournamentCode);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("STANDINGS_DEBUG", "Error checking update time for " + tournamentCode, e);
                    Log.d("STANDINGS_DEBUG", "Running update due to Firestore error");
                    onUpdateNeeded.run(); // Call API on error to be safe
                });
    }
}