package com.kimothorick.soloshelf.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress

@Database(entities = [Book::class, ReadingProgress::class, Bookmark::class, Highlight::class, Note::class], version = 1, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        fun getDatabase(
            context: Context,
        ): BookDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        BookDatabase::class.java,
                        "soloshelf_database",
                    ).build()
                INSTANCE = instance
                instance
            }
    }
}
