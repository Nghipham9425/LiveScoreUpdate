package com.sinhvien.livescore.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.sinhvien.livescore.FireBase.FirebaseHelper;
import com.sinhvien.livescore.Models.Match;
import com.sinhvien.livescore.Adapters.MatchAdapter;
import com.sinhvien.livescore.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MatchAdapter matchAdapter;
    private FirebaseHelper firebaseHelper;
    private Spinner spinnerCompetition;
    private EditText searchTeam;
    private List<Match> matchList = new ArrayList<>();
    private List<Match> allMatches = new ArrayList<>(); // Store all matches

    // Date filtering components
    private Button btnPrevDate, btnNextDate;
    private TextView tvCurrentDate;
    private Calendar currentDate = null;
    private SimpleDateFormat displayDateFormat;
    private SimpleDateFormat apiDateFormat;
    private String[] leagues;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        spinnerCompetition = view.findViewById(R.id.spinnerLeague);
        searchTeam = view.findViewById(R.id.searchTeam);
        firebaseHelper = new FirebaseHelper();

        // Initialize date controls
        btnPrevDate = view.findViewById(R.id.btnPrevDate);
        btnNextDate = view.findViewById(R.id.btnNextDate);
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);

        displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get UID
        String uid = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Initialize adapter
        matchAdapter = new MatchAdapter(getContext(), matchList, uid);
        recyclerView.setAdapter(matchAdapter);

        setupDateControls();
        setupSpinner();
        setupSearch();

        return view;
    }

    private void setupDateControls() {
        // Date navigation buttons
        btnPrevDate.setOnClickListener(v -> {
            if (currentDate == null) {
                // First time selecting a date - default to today
                currentDate = Calendar.getInstance();
            }
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            filterMatchesByDate();
        });

        btnNextDate.setOnClickListener(v -> {
            if (currentDate == null) {
                // First time selecting a date - default to today
                currentDate = Calendar.getInstance();
            }
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            filterMatchesByDate();
        });

        // Reset to show all matches
        tvCurrentDate.setOnClickListener(v -> {
            currentDate = null;
            tvCurrentDate.setText("All Matches");
            matchList.clear();
            matchList.addAll(allMatches);
            matchAdapter.updateData(matchList);
        });
    }

    private void updateDateDisplay() {
        if (currentDate != null) {
            tvCurrentDate.setText(displayDateFormat.format(currentDate.getTime()));
        }
    }

    private void setupSpinner() {
        leagues = new String[]{"Premier League", "Primera Division", "Serie A", "Bundesliga", "Ligue 1", "UEFA Champions League"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, leagues);
        spinnerCompetition.setAdapter(adapter);

        spinnerCompetition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLeague = leagues[position];
                loadMatches(selectedLeague);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadMatches(String competitionName) {
        firebaseHelper.listenForMatchUpdates(competitionName, updatedMatches -> {
            // Store complete list of matches
            allMatches.clear();
            allMatches.addAll(updatedMatches);

            if (currentDate == null) {
                // No date filter - show all matches
                matchList.clear();
                matchList.addAll(allMatches);
            } else {
                // Apply date filter
                filterMatchesByDate();
            }

            matchAdapter.updateData(matchList);
        });
    }

    private void filterMatchesByDate() {
        if (currentDate == null) return;

        String dateString = apiDateFormat.format(currentDate.getTime());
        System.out.println("Filtering by date: " + dateString);

        // Filter matches by date with timezone conversion
        matchList.clear();
        for (Match match : allMatches) {
            try {
                if (match.getMatchTime() != null) {
                    // Parse the UTC time from match
                    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    Date matchUtcDate = utcFormat.parse(match.getMatchTime());

                    // Convert to Vietnam timezone for comparison
                    SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    localDateFormat.setTimeZone(TimeZone.getDefault()); // Vietnam timezone

                    String localMatchDate = localDateFormat.format(matchUtcDate);

                    System.out.println("Match: " + match.getHomeTeam().getName() + " vs " + match.getAwayTeam().getName());
                    System.out.println("UTC time: " + match.getMatchTime() + ", Local date: " + localMatchDate);

                    if (localMatchDate.equals(dateString)) {
                        matchList.add(match);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        matchAdapter.updateData(matchList);
        System.out.println("Found " + matchList.size() + " matches for date " + dateString);
    }

    private void setupSearch() {
        searchTeam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (matchAdapter != null) {
                    matchAdapter.filterMatches(s.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}