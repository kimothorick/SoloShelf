package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kimothorick.soloshelf.data.Converters

@Entity(
    tableName = "books",
    indices = [Index(value = ["filePath"], unique = true)]
)
@TypeConverters(Converters::class)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long = 0,
    val filePath: String,
    val title: String,
    val author: String,
    val format: String,
    val totalPages: Int = 0,
    val coverPath: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val synopsis: String? = null,
    val publicationDate: String? = null,
    val language: String? = null,
    val genres: List<String> = emptyList(),
    val seriesName: String? = null,
    val seriesPosition: Float? = null,
)
