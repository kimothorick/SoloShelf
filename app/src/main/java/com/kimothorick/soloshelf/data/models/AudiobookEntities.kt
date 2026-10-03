package com.kimothorick.soloshelf.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ChapterId(val value: String)

@Serializable
data class MarkData(
    val name: String?,
    val startMs: Long,
)

@Entity(tableName = "content2")
data class BookContentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val author: String,
    val chapters: List<ChapterId>,
    val currentChapter: String,
    val positionInChapter: Long = 0L,
    val isActive: Boolean = true,
)

@Entity(tableName = "chapters2")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val duration: Long,
    val fileLastModified: Long,
    val fileSize: Long,
    val markData: List<MarkData>,
)
