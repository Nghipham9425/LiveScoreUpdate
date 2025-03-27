package com.sinhvien.livescore.Models;

public class MatchNotification {
    private String id;
    private String homeTeam;
    private String awayTeam;
    private String competition;
    private long matchTime;
    private String notificationId;

    public MatchNotification() {
        // Empty constructor for Firebase
    }

    public MatchNotification(String id, String homeTeam, String awayTeam, String competition, long matchTime) {
        this.id = id;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.competition = competition;
        this.matchTime = matchTime;
        this.notificationId = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public String getCompetition() { return competition; }
    public void setCompetition(String competition) { this.competition = competition; }

    public long getMatchTime() { return matchTime; }
    public void setMatchTime(long matchTime) { this.matchTime = matchTime; }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
}