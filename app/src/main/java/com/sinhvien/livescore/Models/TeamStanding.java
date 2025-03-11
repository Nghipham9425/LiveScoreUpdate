package com.sinhvien.livescore.Models;

public class TeamStanding {
    private int position;
    private String teamName;
    private int playedGames;
    private int points;
    private int goalDifference;
    private String teamLogo;

    // 🔥 Thêm constructor rỗng để Firebase có thể tạo object
    public TeamStanding() {}

    public TeamStanding(int position, String teamName, int playedGames, int points, int goalDifference, String teamLogo) {
        this.position = position;
        this.teamName = teamName;
        this.playedGames = playedGames;
        this.points = points;
        this.goalDifference = goalDifference;
        this.teamLogo = teamLogo;
    }

    public int getPosition() { return position; }
    public String getTeamName() { return teamName; }
    public int getPlayedGames() { return playedGames; }
    public int getPoints() { return points; }
    public int getGoalDifference() { return goalDifference; }
    public String getTeamLogo() { return teamLogo; }
}
