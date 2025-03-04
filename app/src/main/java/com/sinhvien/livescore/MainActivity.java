package com.sinhvien.livescore;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private static final String API_TOKEN = "0b0d0150747a44b2b3eeb59b84eb37b4";
    private static final String[] TOURNAMENTS = {"PL", "SA", "BL1", "PD", "FL1", "CL"};
    private List<Match> matchList = new ArrayList<>();
    private MatchDataListener matchDataListener;
    private int tournamentsFetched = 0;
    private FirebaseHelper firebaseHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        firebaseHelper = new FirebaseHelper();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        } else {
            Log.e("MainActivity", "NavHostFragment is NULL!");
        }

        executorService.execute(this::fetchAllMatchData);
    }

    private void fetchAllMatchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            String url = "https://api.football-data.org/v4/competitions/" + tournament + "/matches";
            fetchMatchData(queue, url);
        }
    }

    private void fetchMatchData(RequestQueue queue, String url) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Match> matches = parseMatchResponse(response);
                    if (matches != null) {
                        matchList.addAll(matches);
                        tournamentsFetched++;
                        for (Match match : matches) {
                            firebaseHelper.checkAndSaveMatch(match);
                        }
                        if (tournamentsFetched == TOURNAMENTS.length && matchDataListener != null) {
                            matchDataListener.onMatchDataFetched(matchList);
                        }
                    }
                },
                error -> Log.e("API_ERROR", "Lỗi khi gọi API: " + error.toString())
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

    private List<Match> parseMatchResponse(JSONObject response) {
        List<Match> matches = new ArrayList<>();
        try {
            JSONArray matchesArray = response.getJSONArray("matches");
            JSONObject competitionObject = response.getJSONObject("competition");
            String competitionName = competitionObject.getString("name");

            for (int i = 0; i < matchesArray.length(); i++) {
                JSONObject matchObject = matchesArray.getJSONObject(i);
                JSONObject homeTeamObject = matchObject.getJSONObject("homeTeam");
                JSONObject awayTeamObject = matchObject.getJSONObject("awayTeam");

                String homeTeamName = homeTeamObject.getString("name");
                String awayTeamName = awayTeamObject.getString("name");

                // Lấy logo đội bóng (nếu có)
                String homeCrest = homeTeamObject.has("crest") ? homeTeamObject.getString("crest") : "";
                String awayCrest = awayTeamObject.has("crest") ? awayTeamObject.getString("crest") : "";

                String matchTime = matchObject.getString("utcDate");

                // Kiểm tra điểm số trận đấu
                String score = "Chưa có";
                if (matchObject.has("score") && matchObject.getJSONObject("score").has("fullTime")) {
                    JSONObject fullTime = matchObject.getJSONObject("score").getJSONObject("fullTime");
                    score = fullTime.optString("home", "-") + " - " + fullTime.optString("away", "-");
                }

                Match match = new Match(new Team(homeTeamName, homeCrest), new Team(awayTeamName, awayCrest), score, competitionName, matchTime);
                matches.add(match);
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", "Lỗi phân tích JSON: " + e.getMessage());
        }
        return matches;
    }

    public void setMatchDataListener(MatchDataListener listener) {
        this.matchDataListener = listener;
        if (!matchList.isEmpty()) {
            matchDataListener.onMatchDataFetched(matchList);
        }
    }

    public interface MatchDataListener {
        void onMatchDataFetched(List<Match> matches);
    }
}
