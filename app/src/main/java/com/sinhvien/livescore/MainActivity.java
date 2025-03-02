package com.sinhvien.livescore;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private static final String API_URL = "https://api.football-data.org/v4/competitions/PL/matches";
    private static final String API_TOKEN = "0b0d0150747a44b2b3eeb59b84eb37b4";
    private List<Match> matchList = new ArrayList<>();
    private MatchDataListener matchDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        } else {
            Log.e("MainActivity", "NavHostFragment is NULL!");
        }

        fetchMatchData();
    }

    public void fetchMatchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Dữ liệu trận đấu: " + response.toString());
                        matchList = parseMatchResponse(response);
                        if (matchDataListener != null) {
                            matchDataListener.onMatchDataFetched(matchList);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Lỗi khi gọi API: " + error.toString());
                    }
                }) {
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
            for (int i = 0; i < matchesArray.length(); i++) {
                JSONObject matchObject = matchesArray.getJSONObject(i);
                JSONObject homeTeamObject = matchObject.getJSONObject("homeTeam");
                JSONObject awayTeamObject = matchObject.getJSONObject("awayTeam");
                JSONObject competitionObject = matchObject.getJSONObject("competition");
                JSONObject scoreObject = matchObject.optJSONObject("score");

                String homeTeamName = homeTeamObject.getString("name");
                String homeTeamCrest = homeTeamObject.optString("crest", "");
                String awayTeamName = awayTeamObject.getString("name");
                String awayTeamCrest = awayTeamObject.optString("crest", "");
                String competitionName = competitionObject.getString("name");
                String matchTime = matchObject.getString("utcDate");

                String score = "Chưa có";
                if (scoreObject != null) {
                    JSONObject fullTimeObject = scoreObject.optJSONObject("fullTime");
                    if (fullTimeObject != null) {
                        String homeScore = fullTimeObject.optString("home", "-");
                        String awayScore = fullTimeObject.optString("away", "-");
                        score = homeScore + " - " + awayScore;
                    }
                }

                Team homeTeam = new Team(homeTeamName, homeTeamCrest);
                Team awayTeam = new Team(awayTeamName, awayTeamCrest);
                Match match = new Match(homeTeam, awayTeam, score, competitionName, matchTime);
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
