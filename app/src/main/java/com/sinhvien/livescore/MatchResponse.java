package com.sinhvien.livescore;

import java.util.List;

public class MatchResponse {
    public List<MatchItem> matches;

    public static class MatchItem {
        public Team homeTeam;
        public Team awayTeam;
        public Score score;
        public Competition competition;
        public String utcDate;

        public static class Score {
            public String fullTime; // Tỷ số cả trận
        }

        public static class Competition {
            public String name; // Tên giải đấu
        }
    }
}
