package com.sinhvien.livescore.Repository;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sinhvien.livescore.Models.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsRepository {
    private static final String TAG = "NewsRepository";
    private static final long CACHE_VALIDITY_HOURS = 6; // 6 hours before refreshing from API

    private final FirebaseFirestore db;
    private final CollectionReference newsCollection;

    public NewsRepository() {
        // Simply get the Firestore instance without setting settings
        // Settings are already configured in LiveScoreApplication
        db = FirebaseFirestore.getInstance();
        newsCollection = db.collection("football_news");
        Log.d(TAG, "NewsRepository initialized with collection: football_news");
    }

    public void getNews(NewsCallback callback) {
        Log.d(TAG, "Checking for cached news in Firestore");

        // Check if we have recent news in Firestore
        newsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firestore query successful, documents count: " + task.getResult().size());

                        if (!task.getResult().isEmpty()) {
                            // Found cached data, check if it's fresh enough
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Timestamp timestamp = document.getTimestamp("timestamp");

                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                boolean isFresh = isDataFresh(date);
                                Log.d(TAG, "Cached data timestamp: " + date + ", is fresh: " + isFresh);

                                if (isFresh) {
                                    // Data is fresh, use it
                                    callback.onCachedDataAvailable();
                                    retrieveNewsFromFirestore(callback);
                                } else {
                                    // Data is stale, request fresh data
                                    Log.d(TAG, "Cached data is stale (" + ((System.currentTimeMillis() - date.getTime()) / 3600000) + " hours old), requesting fresh data");
                                    callback.onFreshDataRequired();
                                }
                            } else {
                                Log.w(TAG, "Document has no timestamp field, requesting fresh data");
                                callback.onFreshDataRequired();
                            }
                        } else {
                            // No cached data, request fresh data
                            Log.d(TAG, "No cached news found in Firestore, requesting fresh data");
                            callback.onFreshDataRequired();
                        }
                    } else {
                        Log.e(TAG, "Error checking for cached news", task.getException());
                        callback.onFreshDataRequired();
                    }
                });
    }

    private boolean isDataFresh(Date timestamp) {
        long currentTime = System.currentTimeMillis();
        long cacheTime = timestamp.getTime();
        long cacheValidityMillis = CACHE_VALIDITY_HOURS * 60 * 60 * 1000;
        long ageInMinutes = (currentTime - cacheTime) / 60000;

        Log.d(TAG, "Checking data freshness - age: " + ageInMinutes + " minutes, max allowed: " + (CACHE_VALIDITY_HOURS * 60) + " minutes");
        return (currentTime - cacheTime) < cacheValidityMillis;
    }

    public void saveNewsToFirestore(JSONObject apiResponse) {
        try {
            Log.d(TAG, "Starting to save news to Firestore");

            if (apiResponse == null) {
                Log.e(TAG, "API response is null, cannot save to Firestore");
                return;
            }

            Log.d(TAG, "API response: " + apiResponse.toString().substring(0, Math.min(200, apiResponse.toString().length())) + "...");

            // First delete old news, then save new news AFTER deletion completes
            deleteOldNewsAndThenSaveNew(apiResponse);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error handling news", e);
        }
    }

    private void deleteOldNewsAndThenSaveNew(JSONObject apiResponse) {
        Log.d(TAG, "Deleting old news before saving new ones");

        newsCollection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d(TAG, "Found " + count + " old documents to delete");

                    if (count == 0) {
                        // No documents to delete, proceed with saving
                        saveNewArticles(apiResponse);
                        return;
                    }

                    // Track deletion completion
                    final int[] deletedCount = {0};

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully deleted document: " + document.getId());
                                    deletedCount[0]++;

                                    // After all deletions complete, save new articles
                                    if (deletedCount[0] >= count) {
                                        saveNewArticles(apiResponse);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting document: " + document.getId(), e);
                                    deletedCount[0]++;

                                    // Continue even if some deletions fail
                                    if (deletedCount[0] >= count) {
                                        saveNewArticles(apiResponse);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting documents to delete, saving new articles anyway", e);
                    saveNewArticles(apiResponse);
                });
    }

    private void saveNewArticles(JSONObject apiResponse) {
        try {
            Log.d(TAG, "Now saving new articles after deletion completed");

            JSONArray articles = apiResponse.getJSONArray("articles");
            Log.d(TAG, "Found " + articles.length() + " articles to save");

            for (int i = 0; i < articles.length(); i++) {
                JSONObject article = articles.getJSONObject(i);

                // Log the article being saved (first 100 chars)
                String articlePreview = article.toString().substring(0, Math.min(100, article.toString().length())) + "...";
                Log.d(TAG, "Saving article " + (i+1) + ": " + articlePreview);

                Map<String, Object> newsData = new HashMap<>();
                newsData.put("title", article.getString("title"));
                newsData.put("description", article.getString("description"));
                newsData.put("url", article.getString("url"));
                newsData.put("imageUrl", article.getString("image"));
                newsData.put("publishedAt", article.getString("publishedAt"));
                newsData.put("source", article.getJSONObject("source").getString("name"));
                newsData.put("timestamp", new Date());

                int finalI = i;
                newsCollection.add(newsData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Article " + (finalI+1) + " successfully saved with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error saving article " + (finalI+1) + " to Firestore", e);
                        });
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing API response: " + e.getMessage(), e);
            // Print the entire response to help diagnose the JSON structure
            Log.e(TAG, "API response content: " + (apiResponse != null ? apiResponse.toString() : "null"));
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error saving news", e);
        }
    }

    public void retrieveNewsFromFirestore(NewsCallback callback) {
        Log.d(TAG, "Retrieving news from Firestore");

        newsCollection.orderBy("publishedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d(TAG, "Successfully retrieved " + count + " news items from Firestore");

                    List<News> newsList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String url = document.getString("url");
                            String imageUrl = document.getString("imageUrl");
                            String publishedAt = document.getString("publishedAt");
                            String source = document.getString("source");

                            News news = new News(title, description, url, imageUrl, publishedAt, source);
                            newsList.add(news);
                            Log.d(TAG, "Processed news: " + title);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document: " + document.getId(), e);
                        }
                    }

                    Log.d(TAG, "Total news items loaded: " + newsList.size());
                    callback.onNewsLoaded(newsList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving news from Firestore", e);
                    callback.onError("Failed to load news: " + e.getMessage());
                });
    }

    public interface NewsCallback {
        void onNewsLoaded(List<News> newsList);
        void onError(String errorMessage);
        void onFreshDataRequired();
        void onCachedDataAvailable();
    }
}