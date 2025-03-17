package com.sinhvien.livescore.Models;

import com.sinhvien.livescore.Utils.DateTimeUtils;

public class Match {
    private String matchId;
    private String status;
    private String competition;
    private String score;
    private String matchTime;
    private Team homeTeam;
    private Team awayTeam;
    private boolean isFavorite; // Added isFavorite field

    // Thêm vào lớp Match
    public Match() {
        // Constructor trống cần thiết cho Firestore
        this.isFavorite = false; // Giá trị mặc định
    }
    // Original constructor
    public Match(String matchId, String status, String competition, String score,
                 String matchTime, Team homeTeam, Team awayTeam) {
        this.matchId = matchId;
        this.status = status;
        this.competition = competition;
        this.score = score;
        this.matchTime = matchTime;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.isFavorite = false; // Default value
    }

    // New constructor with isFavorite parameter
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
    }

    // Getters and setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
    public String getFormattedTime() {
        return DateTimeUtils.formatMatchTime(matchTime);
    }
}