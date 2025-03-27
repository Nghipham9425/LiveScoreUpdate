package com.sinhvien.livescore.Models;

import com.sinhvien.livescore.Utils.DateTimeUtils;

public class Match {
    private String matchId;
    private String status;
    private String competition;
    private String score;
    private String matchTime;
    private String date; // Added date field
    private Team homeTeam;
    private Team awayTeam;
    private boolean isFavorite;

    // Empty constructor for Firestore
    public Match() {
        this.isFavorite = false;
    }

    // Constructor without date (for backward compatibility)
    public Match(String matchId, String status, String competition, String score,
                 String matchTime, Team homeTeam, Team awayTeam) {
        this.matchId = matchId;
        this.status = status;
        this.competition = competition;
        this.score = score;
        this.matchTime = matchTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.isFavorite = false;

        // Extract date from matchTime if possible
        if (matchTime != null && matchTime.contains(" ")) {
            this.date = matchTime.split(" ")[0]; // Assuming format "YYYY-MM-DD HH:MM"
        }
    }

    // Constructor with isFavorite
    public Match(String matchId, String status, String competition, String score,
                 String matchTime, Team homeTeam, Team awayTeam, boolean isFavorite) {
        this.matchId = matchId;
        this.status = status;
        this.competition = competition;
        this.score = score;
        this.matchTime = matchTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.isFavorite = isFavorite;

        // Extract date from matchTime if possible
        if (matchTime != null && matchTime.contains(" ")) {
            this.date = matchTime.split(" ")[0]; // Assuming format "YYYY-MM-DD HH:MM"
        }
    }

    // Full constructor with date
    public Match(String matchId, String status, String competition, String score,
                 String matchTime, String date, Team homeTeam, Team awayTeam, boolean isFavorite) {
        this.matchId = matchId;
        this.status = status;
        this.competition = competition;
        this.score = score;
        this.matchTime = matchTime;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.isFavorite = isFavorite;
    }

    // All existing getters and setters...
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCompetition() { return competition; }
    public void setCompetition(String competition) { this.competition = competition; }
    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
    public String getMatchTime() { return matchTime; }
    public void setMatchTime(String matchTime) { this.matchTime = matchTime; }
    public Team getHomeTeam() { return homeTeam; }
    public void setHomeTeam(Team homeTeam) { this.homeTeam = homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public void setAwayTeam(Team awayTeam) { this.awayTeam = awayTeam; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public String getFormattedTime() { return DateTimeUtils.formatMatchTime(matchTime); }

    // New getter and setter for date
    public String getDate() {
        // If date isn't explicitly set, try to extract from matchTime
        if (date == null && matchTime != null && matchTime.contains("T")) {
            return matchTime.split("T")[0]; // Format is "YYYY-MM-DDT..."
        }
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}