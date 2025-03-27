package com.sinhvien.livescore.Activities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.Manifest; // Fix: Correct import for Android permissions
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Models.Standing;
import com.sinhvien.livescore.FireBase.FirebaseHelper;
import com.sinhvien.livescore.Models.Team;
import com.sinhvien.livescore.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private static final String API_TOKEN = "0b0d0150747a44b2b3eeb59b84eb37b4";
    private static final String[] TOURNAMENTS = {"PL", "SA", "BL1", "PD", "FL1", "CL"};
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        firebaseHelper = new FirebaseHelper();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        executorService.execute(() -> {
            fetchAllStandings();
            fetchAllMatches();
        });
        // Trong phương thức onCreate của MainActivity.java
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void applyTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void fetchAllStandings() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            firebaseHelper.shouldUpdateStandings(tournament, () -> fetchStandingsData(queue, tournament));
        }
    }

    private void fetchAllMatches() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            firebaseHelper.shouldUpdateMatches(tournament, () -> fetchMatchesData(queue, tournament));
        }
    }

    private void fetchMatchesData(RequestQueue queue, String tournament) {
        String url = "https://api.football-data.org/v4/competitions/" + tournament + "/matches";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Match> matches = parseMatchesResponse(response, tournament);
                    firebaseHelper.saveMatches(tournament, matches);
                },
                error -> Log.e("API_ERROR", "Error fetching matches: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Auth-Token", API_TOKEN);
                return headers;
            }
        };
        queue.add(request);
    }

    private List<Match> parseMatchesResponse(JSONObject response, String tournament) {
        List<Match> matches = new ArrayList<>();
        try {
            JSONArray matchesArray = response.getJSONArray("matches");
            for (int i = 0; i < matchesArray.length(); i++) {
                JSONObject matchObj = matchesArray.getJSONObject(i);

                String matchId = matchObj.getString("id");
                String status = getMatchStatus(matchObj.getString("status"));
                String score = getScoreString(matchObj.getJSONObject("score"));
                String matchTime = matchObj.getString("utcDate");

                // Get home team data
                JSONObject homeTeamObj = matchObj.getJSONObject("homeTeam");
                Team homeTeam = new Team(
                        homeTeamObj.getString("name"),
                        homeTeamObj.getString("shortName"),
                        homeTeamObj.getString("tla"),
                        homeTeamObj.getString("crest")
                );

                // Get away team data
                JSONObject awayTeamObj = matchObj.getJSONObject("awayTeam");
                Team awayTeam = new Team(
                        awayTeamObj.getString("name"),
                        awayTeamObj.getString("shortName"),
                        awayTeamObj.getString("tla"),
                        awayTeamObj.getString("crest")
                );

                Match match = new Match(
                        matchId,
                        status,
                        tournament,
                        score,
                        matchTime,
                        homeTeam,
                        awayTeam,
                        false  // Default favorite status is false
                );

                matches.add(match);
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE", "Error parsing matches data: " + e.toString());
        }
        return matches;
    }

    private String getMatchStatus(String apiStatus) {
        switch (apiStatus) {
            case "SCHEDULED":
                return "UPCOMING";
            case "LIVE":
            case "IN_PLAY":
            case "PAUSED":
                return "LIVE";
            case "FINISHED":
            case "COMPLETED":
                return "FINISHED";
            default:
                return "UPCOMING";
        }
    }

    private String getScoreString(JSONObject scoreObj) {
        try {
            if (scoreObj.has("fullTime")) {
                JSONObject fullTime = scoreObj.getJSONObject("fullTime");
                if (!fullTime.isNull("home") && !fullTime.isNull("away")) {
                    int home = fullTime.getInt("home");
                    int away = fullTime.getInt("away");
                    return home + " - " + away;
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE", "Error parsing score: " + e.toString());
        }
        return null;
    }

    private void fetchStandingsData(RequestQueue queue, String tournament) {
        String url = "https://api.football-data.org/v4/competitions/" + tournament + "/standings";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Standing> standings = parseStandingsResponse(response);
                    firebaseHelper.saveStandings(tournament, standings);
                },
                error -> Log.e("API_ERROR", "Error fetching standings: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Auth-Token", API_TOKEN);
                return headers;
            }
        };
        queue.add(request);
    }

    private List<Standing> parseStandingsResponse(JSONObject response) {
        List<Standing> standings = new ArrayList<>();
        try {
            JSONArray standingsArray = response.getJSONArray("standings");
            for (int i = 0; i < standingsArray.length(); i++) {
                JSONArray tableArray = standingsArray.getJSONObject(i).getJSONArray("table");
                for (int j = 0; j < tableArray.length(); j++) {
                    JSONObject teamObj = tableArray.getJSONObject(j);
                    int position = teamObj.getInt("position");
                    int playedGames = teamObj.getInt("playedGames");
                    int won = teamObj.getInt("won");
                    int draw = teamObj.getInt("draw");
                    int lost = teamObj.getInt("lost");
                    int points = teamObj.getInt("points");
                    int goalsFor = teamObj.getInt("goalsFor");
                    int goalsAgainst = teamObj.getInt("goalsAgainst");
                    int goalDifference = teamObj.getInt("goalDifference");

                    JSONObject teamInfo = teamObj.getJSONObject("team");
                    Team team = new Team(
                            teamInfo.getString("name"),
                            teamInfo.getString("shortName"),
                            teamInfo.getString("tla"),
                            teamInfo.getString("crest")
                    );

                    Standing standing = new Standing(position, team, playedGames, "",
                            won, draw, lost, points, goalsFor, goalsAgainst, goalDifference);
                    standings.add(standing);
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE", "Error parsing standings data: " + e.toString());
        }
        return standings;
    }
}