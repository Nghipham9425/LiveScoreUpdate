package com.sinhvien.livescore.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;

public class DateTimeUtils {

    /**
     * Convert ISO 8601 timestamp to readable format
     * @param isoTimestamp timestamp in format "2024-08-16T19:00:00Z"
     * @return formatted time like "19:00, 16 Aug"
     */
    public static String formatMatchTime(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) {
            return "TBD";
        }

        try {
            // Parse the ISO timestamp
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(isoTimestamp);

            if (date == null) return "Invalid date";

            // Set to local timezone for display
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());

            return outputFormat.format(date);
        } catch (ParseException e) {
            return "Invalid date format";
        }
    }
}