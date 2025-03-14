package com.sinhvien.livescore.Models;

import java.util.Objects;

public class Match {
    private String matchId;
    private String competition;
    private String matchTime;
    private String score;
    private String status;
    private Team homeTeam;
    private Team awayTeam;

    public Match() {
        // Required empty constructor for Firestore
    }

    public Match(String matchId, String competition, String matchTime, String score,
                 String status, Team homeTeam, Team awayTeam) {
        this.matchId = matchId;
        this.competition = competition;
        this.matchTime = matchTime;
        this.score = score;
        this.status = status;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    // Getters and setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(String matchTime) {
        this.matchTime = matchTime;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}