package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kimothorick.soloshelf.data.Converters
import org.readium.r2.shared.util.mediatype.MediaType

@Entity(
    tableName = "books",
    indices = [Index(value = ["filePath"], unique = true)],
)
@TypeConverters(Converters::class)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long = 0,
    val filePath: String,
    val title: String,
    val author: String,
    val format: String,
    val mediaType: MediaType? = null,
    val coverPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val synopsis: String? = null,
    val publicationDate: String? = null,
    val language: String? = null,
    val genres: List<String> = emptyList(),
    val seriesName: String? = null,
    val seriesPosition: Float? = null,
    val publisher: String? = null,
    val isbn: String? = null,
    val isAudiobook: Boolean = false,
)
