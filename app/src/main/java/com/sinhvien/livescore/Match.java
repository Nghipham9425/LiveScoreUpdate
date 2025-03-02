package com.sinhvien.livescore;

public class Match {
    private Team homeTeam;
    private Team awayTeam;
    private String score;
    private String competition;
    private String time;

    // Constructor
    public Match(Team homeTeam, Team awayTeam, String score, String competition, String time) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.competition = competition;
        this.time = time;
    }

    // Getter methods
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

    public String getTime() {
        return time;
    }
}
