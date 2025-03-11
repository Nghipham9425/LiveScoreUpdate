package com.sinhvien.livescore.Models;

public class Match {
    private String matchId;
    private Team homeTeam;
    private Team awayTeam;
    private String score;
    private String competition;
    private String matchTime;
    private String status;

    public Match()
    {

    }
    public Match(String matchId, Team homeTeam, Team awayTeam, String score, String competition, String matchTime, String status) {
        this.matchId = matchId;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.competition = competition;
        this.matchTime = matchTime;
        this.status = status;
    }

    public String getMatchId() {
        return matchId;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public String getScore() {
        return score;
    }

    public String getCompetition() {
        return competition;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public String getStatus() {
        return status;
    }
}
