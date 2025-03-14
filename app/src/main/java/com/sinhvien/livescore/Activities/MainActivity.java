package com.sinhvien.livescore.Activities;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
        });
    }

    private void fetchAllStandings() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            firebaseHelper.shouldUpdateStandings(tournament, () -> fetchStandingsData(queue, tournament));
        }
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
                    Team team = new Team(teamInfo.getString("name"), teamInfo.getString("crest"));

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
