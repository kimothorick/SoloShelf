package com.kimothorick.soloshelf.data

import androidx.room.TypeConverter
import com.kimothorick.soloshelf.data.models.ChapterId
import com.kimothorick.soloshelf.data.models.MarkData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.readium.r2.shared.util.mediatype.MediaType

class Converters {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromMediaType(value: MediaType?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toMediaType(value: String?): MediaType? {
        return value?.let { MediaType(it) }
    }

    @TypeConverter
    fun fromMarkDataList(value: List<MarkData>?): String {
        return if (value == null) "[]" else json.encodeToString(value)
    }

    @TypeConverter
    fun toMarkDataList(value: String?): List<MarkData> {
        return if (value.isNullOrBlank()) emptyList() else try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromChapterIdList(value: List<ChapterId>?): String {
        return if (value == null) "[]" else json.encodeToString(value)
    }

    @TypeConverter
    fun toChapterIdList(value: String?): List<ChapterId> {
        return if (value.isNullOrBlank()) emptyList() else try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
