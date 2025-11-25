package com.kimothorick.soloshelf.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long = 0,
    @ColumnInfo(index = true)
    val filePath: String,
    val title: String,
    val author: String,
    val format: String,
)
