package com.kimothorick.soloshelf.util

import java.text.NumberFormat
import java.util.Locale

object NumberUtils {
    /**
     * Formats an integer to a localized string (e.g., 1000 -> 1,000 in US).
     */
    fun formatLocalizedInteger(
        value: Int,
        locale: Locale = Locale.getDefault(),
    ): String = NumberFormat.getIntegerInstance(locale).format(value)

    /**
     * Formats a long to a localized string.
     */
    fun formatLocalizedLong(
        value: Long,
        locale: Locale = Locale.getDefault(),
    ): String = NumberFormat.getIntegerInstance(locale).format(value)
}
