package com.kimothorick.soloshelf.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kimothorick.soloshelf.data.models.BookContentEntity
import com.kimothorick.soloshelf.data.models.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookContent(bookContent: BookContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookContents(bookContents: List<BookContentEntity>)

    @Query("SELECT * FROM content2 WHERE id = :id")
    suspend fun getBookContentById(id: String): BookContentEntity?

    @Query("SELECT * FROM content2 WHERE isActive = 1")
    fun getActiveBookContents(): Flow<List<BookContentEntity>>

    @Query("SELECT * FROM content2")
    fun getAllBookContents(): Flow<List<BookContentEntity>>

    @Query("UPDATE content2 SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveStatus(id: String, isActive: Boolean)

    @Query("UPDATE content2 SET currentChapter = :currentChapter, positionInChapter = :positionInChapter WHERE id = :id")
    suspend fun updateProgress(id: String, currentChapter: String, positionInChapter: Long)

    @Query("DELETE FROM content2 WHERE id = :id")
    suspend fun deleteBookContent(id: String)
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters2 WHERE id = :id")
    suspend fun getChapterById(id: String): ChapterEntity?

    @Query("SELECT * FROM chapters2 WHERE id = :id AND fileLastModified = :fileLastModified AND fileSize = :fileSize")
    suspend fun getValidCachedChapter(id: String, fileLastModified: Long, fileSize: Long): ChapterEntity?

    @Query("SELECT * FROM chapters2 WHERE id IN (:ids)")
    suspend fun getChaptersByIds(ids: List<String>): List<ChapterEntity>

    @Query("DELETE FROM chapters2 WHERE id = :id")
    suspend fun deleteChapter(id: String)
}
