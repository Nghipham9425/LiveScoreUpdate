package com.sinhvien.livescore.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Models.Team;
import com.sinhvien.livescore.R;

import java.util.HashMap;
import java.util.Map;

public class MatchDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MatchDetailsActivity";

    private TextView tvCompetition, tvStatus, tvDate;
    private TextView tvHomeTeam, tvAwayTeam, tvScore;
    private TextView tvHomeTLA, tvAwayTLA; // Added TLA TextViews
    private TextView tvHomeTeamName, tvAwayTeamName, tvCompetitionInfo, tvStatusInfo, tvMatchId, tvTimeInfo;
    private ImageView ivHomeTeam, ivAwayTeam, ivCompetitionLogo;
    private ImageView ivBack;
    private LinearLayout matchInfoContainer;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String matchId;
    private Match currentMatch;

    // Competition code to full name mapping
    private static final Map<String, String> competitionNames = new HashMap<>();
    static {
        competitionNames.put("PL", "Premier League");
        competitionNames.put("CL", "UEFA Champions League");
        competitionNames.put("BL1", "Bundesliga");
        competitionNames.put("SA", "Serie A");
        competitionNames.put("PD", "Primera Division");
        competitionNames.put("FL1", "Ligue 1");
        competitionNames.put("EC", "European Championship");
        competitionNames.put("WC", "World Cup");
        competitionNames.put("ELC", "Championship");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get match ID from intent
        matchId = getIntent().getStringExtra("match_id");
        if (matchId == null || matchId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin trận đấu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        fetchMatchFromFirestore();
    }

    private void initViews() {
        // Toolbar
        ivBack = findViewById(R.id.ivBack);

        // Competition
        tvCompetition = findViewById(R.id.tvCompetition);
        ivCompetitionLogo = findViewById(R.id.ivCompetitionLogo);
        tvCompetitionInfo = findViewById(R.id.tvCompetitionInfo);

        // Status
        tvStatus = findViewById(R.id.tvStatus);
        tvStatusInfo = findViewById(R.id.tvStatusInfo);

        // Match ID
        tvMatchId = findViewById(R.id.tvMatchId);

        // Date
        tvDate = findViewById(R.id.tvDate);
        tvTimeInfo = findViewById(R.id.tvTimeInfo);

        // Score
        tvScore = findViewById(R.id.tvScore);

        // Teams
        tvHomeTeam = findViewById(R.id.tvHomeTeam);
        tvAwayTeam = findViewById(R.id.tvAwayTeam);
        tvHomeTLA = findViewById(R.id.tvHomeTLA); // Initialize TLA TextViews
        tvAwayTLA = findViewById(R.id.tvAwayTLA); // Initialize TLA TextViews
        tvHomeTeamName = findViewById(R.id.tvHomeTeamName);
        tvAwayTeamName = findViewById(R.id.tvAwayTeamName);
        ivHomeTeam = findViewById(R.id.ivHomeTeam);
        ivAwayTeam = findViewById(R.id.ivAwayTeam);

        // Container and progress
        matchInfoContainer = findViewById(R.id.matchInfoContainer);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    private void fetchMatchFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        matchInfoContainer.setVisibility(View.INVISIBLE);

        // Log the matchId for debugging
        Log.d(TAG, "Attempting to fetch match with ID: " + matchId);

        db.collection("Matches").document(matchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Match document found!");
                        Match match = documentSnapshot.toObject(Match.class);
                        if (match != null) {
                            currentMatch = match;
                            displayMatchDetails(match);
                            logMatchData(match);
                        } else {
                            Log.e(TAG, "Failed to convert document to Match object");
                            Toast.makeText(this, "Không thể đọc dữ liệu trận đấu", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.e(TAG, "Document does not exist at path: Matches/" + matchId);

                        // Try an alternative approach - query by matchId field
                        db.collection("Matches")
                                .whereEqualTo("matchId", matchId)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        Log.d(TAG, "Found match using query instead!");
                                        Match match = querySnapshot.getDocuments().get(0).toObject(Match.class);
                                        currentMatch = match;
                                        displayMatchDetails(match);
                                        logMatchData(match);
                                    } else {
                                        Log.e(TAG, "Match not found in the database");
                                        Toast.makeText(this, "Không tìm thấy trận đấu", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching match: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayMatchDetails(Match match) {
        try {
            // Competition
            String competitionCode = match.getCompetition();
            String fullCompetitionName = getCompetitionFullName(competitionCode);
            tvCompetition.setText(fullCompetitionName);
            tvCompetitionInfo.setText(fullCompetitionName);

            // Load competition logo
            String competitionLogoUrl = getCompetitionLogo(competitionCode);
            Glide.with(this)
                    .load(competitionLogoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_placeholder)
                    .into(ivCompetitionLogo);

            // Status
            String statusText = getStatusTranslation(match.getStatus());
            tvStatus.setText(statusText);
            tvStatusInfo.setText(statusText);

            // Match ID
            tvMatchId.setText(match.getMatchId());

            // Time
            tvDate.setText(match.getFormattedTime());
            tvTimeInfo.setText(match.getFormattedTime());

            // Score
            tvScore.setText(match.getScore());

            // Teams
            Team homeTeam = match.getHomeTeam();
            Team awayTeam = match.getAwayTeam();

            if (homeTeam != null) {
                tvHomeTeam.setText(homeTeam.getShortName());
                tvHomeTLA.setText(homeTeam.getTla());  // Set the TLA
                tvHomeTeamName.setText(homeTeam.getName());

                Glide.with(this)
                        .load(homeTeam.getCrest())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.ic_placeholder)
                        .into(ivHomeTeam);
            }

            if (awayTeam != null) {
                tvAwayTeam.setText(awayTeam.getShortName());
                tvAwayTLA.setText(awayTeam.getTla());  // Set the TLA
                tvAwayTeamName.setText(awayTeam.getName());

                Glide.with(this)
                        .load(awayTeam.getCrest())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .error(R.drawable.ic_placeholder)
                        .into(ivAwayTeam);
            }

            // Show the content
            progressBar.setVisibility(View.GONE);
            matchInfoContainer.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error displaying match details: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị chi tiết trận đấu", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCompetitionFullName(String code) {
        return competitionNames.getOrDefault(code, code);
    }

    private String getStatusTranslation(String status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case "SCHEDULED": return "Sắp diễn ra";
            case "LIVE": return "Đang diễn ra";
            case "IN_PLAY": return "Đang diễn ra";
            case "PAUSED": return "Tạm dừng";
            case "FINISHED": return "Đã kết thúc";
            case "POSTPONED": return "Hoãn lại";
            case "SUSPENDED": return "Tạm hoãn";
            case "CANCELED": return "Đã hủy";
            default: return status;
        }
    }

    private void logMatchData(Match match) {
        if (match == null) return;

        Log.d(TAG, "Match ID: " + match.getMatchId());
        Log.d(TAG, "Competition: " + match.getCompetition());
        Log.d(TAG, "Status: " + match.getStatus());
        Log.d(TAG, "Score: " + match.getScore());
        Log.d(TAG, "Time: " + match.getMatchTime());
        Log.d(TAG, "Formatted Time: " + match.getFormattedTime());

        if (match.getHomeTeam() != null) {
            Log.d(TAG, "Home Team: " + match.getHomeTeam().getName());
        }

        if (match.getAwayTeam() != null) {
            Log.d(TAG, "Away Team: " + match.getAwayTeam().getName());
        }
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }

    private String getCompetitionLogo(String competition) {
        // Map competition codes to their logo URLs
        switch (competition) {
            case "PL":
                return "https://crests.football-data.org/PL.png";
            case "CL":
                return "https://crests.football-data.org/CL.png";
            case "BL1":
                return "https://crests.football-data.org/BL1.png";
            case "SA":
                return "https://crests.football-data.org/SA.png";
            case "PD":
                return "https://crests.football-data.org/PD.png";
            case "FL1":
                return "https://crests.football-data.org/FL1.png";
            case "EC":
                return "https://crests.football-data.org/EUR.png";
            case "WC":
                return "https://crests.football-data.org/WC.png";
            case "ELC":
                return "https://crests.football-data.org/ELC.png";
            default:
                return "https://crests.football-data.org/default.png";
        }
    }
}