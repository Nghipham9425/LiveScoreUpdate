package com.sinhvien.livescore;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private final FirebaseFirestore db;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void checkAndSaveMatch(Match match) {
        // Định nghĩa ID duy nhất cho mỗi trận đấu (VD: kết hợp đội và thời gian)
        String matchId = match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName() + " " + match.getTime();

        Map<String, Object> matchData = new HashMap<>();
        matchData.put("homeTeam", match.getHomeTeam().getName());
        matchData.put("awayTeam", match.getAwayTeam().getName());
        matchData.put("score", match.getScore());
        matchData.put("competition", match.getCompetition());
        matchData.put("matchTime", match.getTime());

        db.collection("matches").document(matchId)
                .set(matchData)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Thành công: Lưu trận đấu vào Firestore"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Lỗi khi lưu: ", e));
    }
}
