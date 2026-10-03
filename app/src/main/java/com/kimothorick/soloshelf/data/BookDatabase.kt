package com.kimothorick.soloshelf.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kimothorick.soloshelf.data.daos.BookDao
import com.kimothorick.soloshelf.data.models.Book
import com.kimothorick.soloshelf.data.models.Bookmark
import com.kimothorick.soloshelf.data.models.Collection
import com.kimothorick.soloshelf.data.models.CollectionBookCrossRef
import com.kimothorick.soloshelf.data.models.Highlight
import com.kimothorick.soloshelf.data.models.Note
import com.kimothorick.soloshelf.data.models.ReadingProgress
import com.kimothorick.soloshelf.data.models.Track
import com.kimothorick.soloshelf.data.models.AudiobookDetails
import com.kimothorick.soloshelf.data.models.EbookDetails
import com.kimothorick.soloshelf.data.models.AudioProgress
import com.kimothorick.soloshelf.data.models.BookContentEntity
import com.kimothorick.soloshelf.data.models.ChapterEntity
import com.kimothorick.soloshelf.data.daos.BookContentDao
import com.kimothorick.soloshelf.data.daos.ChapterDao

@Database(
    entities = [
        Book::class,
        ReadingProgress::class,
        Bookmark::class,
        Highlight::class,
        Note::class,
        Collection::class,
        CollectionBookCrossRef::class,
        Track::class,
        AudiobookDetails::class,
        EbookDetails::class,
        AudioProgress::class,
        BookContentEntity::class,
        ChapterEntity::class,
    ],
    version = 15,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookContentDao(): BookContentDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        @Volatile
        private var INSTANCE: BookDatabase? = null

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(
                db: SupportSQLiteDatabase,
            ) {
                db.execSQL("ALTER TABLE books ADD COLUMN publisher TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN isbn TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(
                db: SupportSQLiteDatabase,
            ) {
                db.execSQL("ALTER TABLE books ADD COLUMN isAudiobook INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS `tracks` (`trackId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `title` TEXT NOT NULL, `duration` INTEGER NOT NULL, `trackNumber` INTEGER NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `books`(`bookId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tracks_bookId` ON `tracks` (`bookId`)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new tables
                db.execSQL("CREATE TABLE IF NOT EXISTS `audiobook_details` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `narrator` TEXT, `totalDurationMs` INTEGER NOT NULL, `bitrate` INTEGER, `samplingRate` INTEGER, FOREIGN KEY(`bookId`) REFERENCES `books`(`bookId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_audiobook_details_bookId` ON `audiobook_details` (`bookId`)")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `ebook_details` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `pageCount` INTEGER NOT NULL, `hasImages` INTEGER NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `books`(`bookId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ebook_details_bookId` ON `ebook_details` (`bookId`)")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `audio_progress` (`bookId` INTEGER PRIMARY KEY NOT NULL, `currentTrackId` INTEGER, `currentOffsetMs` INTEGER NOT NULL, `lastAccessed` INTEGER NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `books`(`bookId`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_audio_progress_bookId` ON `audio_progress` (`bookId`)")

                // Migrate data from totalPages to new tables
                // For Ebooks
                db.execSQL("INSERT INTO ebook_details (bookId, pageCount, hasImages) SELECT bookId, totalPages, 0 FROM books WHERE isAudiobook = 0")
                // For Audiobooks (convert seconds to ms)
                db.execSQL("INSERT INTO audiobook_details (bookId, totalDurationMs) SELECT bookId, totalPages * 1000 FROM books WHERE isAudiobook = 1")

                // Remove totalPages from books (Recreate table approach)
                db.execSQL("CREATE TABLE `books_new` (`bookId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `filePath` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `format` TEXT NOT NULL, `coverPath` TEXT, `dateAdded` INTEGER NOT NULL, `synopsis` TEXT, `publicationDate` TEXT, `language` TEXT, `genres` TEXT NOT NULL, `seriesName` TEXT, `seriesPosition` REAL, `publisher` TEXT, `isbn` TEXT, `isAudiobook` INTEGER NOT NULL)")
                db.execSQL("INSERT INTO books_new (bookId, filePath, title, author, format, coverPath, dateAdded, synopsis, publicationDate, language, genres, seriesName, seriesPosition, publisher, isbn, isAudiobook) SELECT bookId, filePath, title, author, format, coverPath, dateAdded, synopsis, publicationDate, language, genres, seriesName, seriesPosition, publisher, isbn, isAudiobook FROM books")
                db.execSQL("DROP TABLE books")
                db.execSQL("ALTER TABLE books_new RENAME TO books")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_books_filePath` ON `books` (`filePath`)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN mediaType TEXT")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tracks ADD COLUMN startMs INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tracks ADD COLUMN endMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(
            context: Context,
        ): BookDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        BookDatabase::class.java,
                        "soloshelf_database",
                    ).addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_14_15)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
    }
}
