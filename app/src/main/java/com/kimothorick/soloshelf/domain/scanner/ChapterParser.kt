package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.net.Uri
import com.kimothorick.soloshelf.data.daos.ChapterDao
import com.kimothorick.soloshelf.data.models.ChapterEntity

class ChapterParser(
    private val context: Context,
    private val mediaAnalyzer: MediaAnalyzer,
    private val chapterDao: ChapterDao? = null,
    private val cache: ChapterCacheRepository = InMemoryChapterCacheRepository(),
) {
    interface ChapterCacheRepository {
        suspend fun get(
            id: String,
            lastModified: Long,
            size: Long,
        ): ChapterRecord?

        suspend fun put(
            record: ChapterRecord,
        )
    }

    class InMemoryChapterCacheRepository : ChapterCacheRepository {
        private val memoryMap = mutableMapOf<String, ChapterRecord>()

        override suspend fun get(
            id: String,
            lastModified: Long,
            size: Long,
        ): ChapterRecord? {
            val record = memoryMap[id]
            return if (record != null && record.fileLastModified == lastModified && record.fileSize == size) {
                record
            } else {
                null
            }
        }

        override suspend fun put(
            record: ChapterRecord,
        ) {
            memoryMap[record.id] = record
        }
    }

    suspend fun parseDirectory(
        targetUri: Uri,
    ): List<ChapterRecord> {
        val files = mutableListOf<CachedDocumentFile>()
        traverseUri(targetUri, files)

        val audioFiles = files.filter { CachedDocumentFile.isAudioExtension(it.extension) }
        val comparator = NaturalOrderComparator()
        val sortedAudioFiles = audioFiles.sortedWith { f1, f2 ->
            comparator.compare(f1.name, f2.name)
        }

        val chapters = mutableListOf<ChapterRecord>()
        for (file in sortedAudioFiles) {
            val id = file.uri.toString()
            var cachedRecord: ChapterRecord? = null

            // Check DB cache if available
            if (chapterDao != null) {
                val dbEntity = chapterDao.getValidCachedChapter(id, file.lastModified, file.size)
                if (dbEntity != null) {
                    cachedRecord = ChapterRecord(
                        id = dbEntity.id,
                        uri = file.uri,
                        name = dbEntity.name,
                        fileLastModified = dbEntity.fileLastModified,
                        fileSize = dbEntity.fileSize,
                        metadata = AnalyzedMetadata(
                            title = dbEntity.name,
                            durationMs = dbEntity.duration,
                            chapters = dbEntity.markData.map { ChapterMark(name = it.name ?: "Chapter", startMs = it.startMs, endMs = 0L) },
                        ),
                    )
                }
            }

            // Fallback to in-memory cache
            if (cachedRecord == null) {
                cachedRecord = cache.get(id, file.lastModified, file.size)
            }

            val record = if (cachedRecord != null) {
                cachedRecord
            } else {
                val analyzed = mediaAnalyzer.analyze(file.uri, file.name)
                val newRecord = ChapterRecord(
                    id = id,
                    uri = file.uri,
                    name = file.name,
                    fileLastModified = file.lastModified,
                    fileSize = file.size,
                    metadata = analyzed,
                )
                cache.put(newRecord)
                if (chapterDao != null) {
                    chapterDao.insertChapter(
                        ChapterEntity(
                            id = newRecord.id,
                            name = newRecord.name,
                            duration = newRecord.metadata.durationMs,
                            fileLastModified = newRecord.fileLastModified,
                            fileSize = newRecord.fileSize,
                            markData = newRecord.metadata.chapters.map {
                                com.kimothorick.soloshelf.data.models
                                    .MarkData(name = it.name, startMs = it.startMs)
                            },
                        ),
                    )
                }
                newRecord
            }
            chapters.add(record)
        }

        return chapters
    }

    private fun traverseUri(
        uri: Uri,
        acc: MutableList<CachedDocumentFile>,
    ) {
        val items = CachedDocumentFile.listFiles(context, uri)
        if (items.isEmpty()) {
            // Single file check
            val single = CachedDocumentFile.fromDocumentFile(
                androidx.documentfile.provider.DocumentFile
                    .fromSingleUri(context, uri) ?: return,
            )
            acc.add(single)
        } else {
            items.forEach { item ->
                if (item.isDirectory) {
                    traverseUri(item.uri, acc)
                } else {
                    acc.add(item)
                }
            }
        }
    }
}
