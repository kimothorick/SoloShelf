package com.kimothorick.soloshelf.util

import android.content.Context
import com.kimothorick.soloshelf.R
import java.util.Locale

object TimeUtils {
    /**
     * Formats seconds into HH:MM:SS format.
     */
    fun formatHHMMSS(
        seconds: Long,
    ): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    /**
     * Formats duration in milliseconds into a human readable string like "12h 43m" or "43m".
     */
    fun formatDuration(
        durationMs: Long,
    ): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    /**
     * Returns a time-based greeting message ("Good Morning", "Good Afternoon", or "Good Evening").
     */
    fun getGreeting(context: Context): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> context.getString(R.string.good_morning_greeting)
            in 12..16 -> context.getString(R.string.good_afternoon_greeting)
            else -> context.getString(R.string.good_evening_greeting)
        }
    }
}
