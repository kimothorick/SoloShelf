package com.kimothorick.soloshelf.data

import androidx.room.TypeConverter
import org.readium.r2.shared.util.mediatype.MediaType

class Converters {
    @TypeConverter
    fun fromStringList(
        value: List<String>,
    ): String = value.joinToString(separator = ",")

    @TypeConverter
    fun toStringList(
        value: String,
    ): List<String> = if (value.isBlank()) emptyList() else value.split(",")

    @TypeConverter
    fun fromMediaType(
        value: MediaType?,
    ): String? = value?.toString()

    @TypeConverter
    fun toMediaType(
        value: String?,
    ): MediaType? = value?.let { MediaType(it) }
}
