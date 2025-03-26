package com.sinhvien.livescore.Network;

import com.sinhvien.livescore.Models.Competition;
import com.sinhvien.livescore.Models.MatchResponse;
import com.sinhvien.livescore.Models.StandingsResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FootballApiService {

    @GET("competitions")
    Call<List<Competition>> getCompetitions(); // API lấy danh sách các giải đấu

    @GET("competitions/{competitionId}/matches")
    Call<MatchResponse> getMatchesForCompetition(
            @Path("competitionId") String competitionId, // ID giải đấu
            @Query("season") int season, // Mùa giải
            @Query("dateFrom") String dateFrom, // Ngày bắt đầu
            @Query("dateTo") String dateTo // Ngày kết thúc
    );




    @GET("competitions/{competitionId}/standings")
    Call<StandingsResponse> getStandings(
            @Path("competitionId") String competitionId
    );

}
