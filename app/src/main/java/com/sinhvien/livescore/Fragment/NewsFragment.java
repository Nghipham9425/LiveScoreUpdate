package com.sinhvien.livescore.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sinhvien.livescore.Adapters.NewsAdapter;
import com.sinhvien.livescore.Models.News;
import com.sinhvien.livescore.R;
import com.sinhvien.livescore.Repository.NewsRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment implements NewsRepository.NewsCallback {
    private static final String TAG = "NewsFragment";
    private static final String NEWS_API_KEY = "2a6aa1c0fd63f6ad37112361fba4ea92";

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<News> newsList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RequestQueue requestQueue;
    private NewsRepository newsRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        Log.d(TAG, "NewsFragment created");

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerView.setAdapter(newsAdapter);

        requestQueue = Volley.newRequestQueue(requireContext());
        newsRepository = new NewsRepository();

        Log.d(TAG, "NewsRepository initialized");

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Manual refresh triggered");
            fetchNewsFromApi();
        });

        loadNews();

        return view;
    }

    private void loadNews() {
        if (getView() != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Loading news data");
        newsRepository.getNews(this);
    }

    @Override
    public void onNewsLoaded(List<News> loadedNewsList) {
        if (getView() == null || !isAdded()) {
            return; // Fragment is detached, don't update UI
        }

        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        Log.d(TAG, "News loaded from Firestore, count: " + loadedNewsList.size());

        newsList.clear();
        newsList.addAll(loadedNewsList);
        newsAdapter.notifyDataSetChanged();

        if (getContext() != null) {
            Toast.makeText(getContext(), "Loaded " + loadedNewsList.size() + " news articles", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(String errorMessage) {
        Log.e(TAG, "Error loading news: " + errorMessage);

        if (getView() == null || !isAdded()) {
            return; // Fragment is detached, don't update UI
        }

        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (getContext() != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFreshDataRequired() {
        Log.d(TAG, "Fresh data required - fetching from API");
        if (isAdded()) { // Only fetch if fragment is still attached
            fetchNewsFromApi();
        }
    }

    @Override
    public void onCachedDataAvailable() {
        Log.d(TAG, "Cached data available - using Firestore data");

        if (isAdded() && getContext() != null) {
            Toast.makeText(getContext(), "Using cached football news", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNewsFromApi() {
        if (getView() == null || !isAdded()) {
            return; // Don't fetch if fragment is detached
        }

        progressBar.setVisibility(View.VISIBLE);

        // Football-focused search query
        String url = "https://gnews.io/api/v4/search?q=football%20OR%20soccer%20OR%20premier%20league%20OR%20champions%20league&lang=en&country=us&max=10&apikey=" + NEWS_API_KEY;

        Log.d(TAG, "Fetching news from API: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "API response received successfully");

                    // Early return if fragment is detached
                    if (getView() == null || !isAdded()) {
                        Log.d(TAG, "Fragment detached, not updating UI");
                        return;
                    }

                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Saving news to Firestore...", Toast.LENGTH_SHORT).show();
                    }

                    // Save to Firestore
                    newsRepository.saveNewsToFirestore(response);

                    // Load the saved news
                    newsRepository.retrieveNewsFromFirestore(this);
                },
                error -> {
                    Log.e(TAG, "Error fetching news: " + error.toString());

                    // Early return if fragment is detached
                    if (getView() == null || !isAdded()) {
                        Log.d(TAG, "Fragment detached, not updating UI");
                        return;
                    }

                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error fetching news: " +
                                        (error.getMessage() != null ? error.getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }

                    // Try to load any previously cached news as fallback
                    newsRepository.retrieveNewsFromFirestore(this);
                }
        );

        // Set a longer timeout and fewer retries
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                1,     // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}