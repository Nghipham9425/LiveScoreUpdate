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
import com.sinhvien.livescore.R;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

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

        executorService.execute(this::fetchAllMatchData);
        executorService.execute(this::fetchAllStandings);
    }

    private void fetchAllMatchData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            fetchMatchData(queue, tournament);
        }
    }

    private void fetchMatchData(RequestQueue queue, String tournament) {
        String url = "https://api.football-data.org/v4/competitions/" + tournament + "/matches";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Match> matches = parseMatchResponse(response);
                    for (Match match : matches) {
                        firebaseHelper.saveOrUpdateMatch(match);
                    }
                },
                error -> Log.e("API_ERROR", "Error fetching API: " + error.toString())
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
        // 📝 Code xử lý JSON...
        return matches;
    }

    private void fetchAllStandings() {
        RequestQueue queue = Volley.newRequestQueue(this);
        for (String tournament : TOURNAMENTS) {
            fetchStandingsData(queue, tournament);
        }
    }

    private void fetchStandingsData(RequestQueue queue, String tournament) {
        String url = "https://api.football-data.org/v4/competitions/" + tournament + "/standings";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
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
        // 📝 Code xử lý JSON để lấy danh sách bảng xếp hạng
        return standings;
    }
}
