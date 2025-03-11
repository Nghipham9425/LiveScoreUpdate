package com.sinhvien.livescore.Models;

import java.util.List;

public class StandingsResponse {
    private List<StandingTable> standings;

    public List<StandingTable> getStandings() {
        return standings;
    }

    public static class StandingTable {
        private List<Standing> table;

        public List<Standing> getTable() {
            return table;
        }
    }
}
