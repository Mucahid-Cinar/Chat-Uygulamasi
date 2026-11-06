package com.mucahid.mm_chat.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeUtils {

    private TimeUtils() {
    }

    /** Current time as 24-hour "HH:mm". */
    public static String currentTime() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    /** Current date as "dd.MM.yyyy". */
    public static String currentDate() {
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
    }

    /** "HH:mm" if the timestamp is today, otherwise "dd.MM.yyyy HH:mm". */
    public static String formatLastSeen(long timestampMillis) {
        Date date = new Date(timestampMillis);
        String day = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String pattern = day.equals(today) ? "HH:mm" : "dd.MM.yyyy HH:mm";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}
