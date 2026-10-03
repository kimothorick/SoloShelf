package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import android.util.Log

class CachedDocumentFile private constructor(
    val uri: Uri,
    val name: String,
    val isDirectory: Boolean,
    val lastModified: Long,
    val size: Long,
) {
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()

    fun cleanedName(): String {
        return name.substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
    }

    companion object {
        private val AUDIO_EXTENSIONS = setOf("mp3", "m4b", "m4a", "flac", "ogg", "opus", "aac")
        private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

        fun isAudioExtension(ext: String): Boolean = AUDIO_EXTENSIONS.contains(ext.lowercase())
        fun isImageExtension(ext: String): Boolean = IMAGE_EXTENSIONS.contains(ext.lowercase())

        fun fromDocumentFile(doc: DocumentFile): CachedDocumentFile {
            return CachedDocumentFile(
                uri = doc.uri,
                name = doc.name ?: "Unknown",
                isDirectory = doc.isDirectory,
                lastModified = doc.lastModified(),
                size = doc.length(),
            )
        }

        fun listDirectoryFiles(context: Context, uri: Uri): List<CachedDocumentFile> {
            val result = mutableListOf<CachedDocumentFile>()
            try {
                val doc = DocumentFile.fromSingleUri(context, uri) ?: DocumentFile.fromTreeUri(context, uri)
                val targetDir = if (doc != null && !doc.isDirectory) doc.parentFile else doc
                if (targetDir != null && targetDir.isDirectory) {
                    targetDir.listFiles().forEach { file ->
                        result.add(fromDocumentFile(file))
                    }
                    return result
                }
            } catch (e: Exception) {
                Log.e("CachedDocumentFile", "Error listing directory files for $uri", e)
            }
            return listFiles(context, uri)
        }

        fun listFiles(context: Context, parentUri: Uri): List<CachedDocumentFile> {
            val result = mutableListOf<CachedDocumentFile>()
            
            val singleDoc = DocumentFile.fromSingleUri(context, parentUri)
            if (singleDoc != null && !singleDoc.isDirectory) {
                result.add(fromDocumentFile(singleDoc))
                return result
            }

            try {
                val docId = if (DocumentsContract.isDocumentUri(context, parentUri)) {
                    DocumentsContract.getDocumentId(parentUri)
                } else {
                    DocumentsContract.getTreeDocumentId(parentUri)
                }
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri, docId)
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_SIZE,
                )

                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                    while (cursor.moveToNext()) {
                        val childDocId = if (idCol != -1) cursor.getString(idCol) else null
                        val name = if (nameCol != -1) cursor.getString(nameCol) else "Unknown"
                        val mime = if (mimeCol != -1) cursor.getString(mimeCol) else ""
                        val lastModified = if (modCol != -1) cursor.getLong(modCol) else 0L
                        val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                        val isDir = mime == DocumentsContract.Document.MIME_TYPE_DIR

                        val childUri = if (childDocId != null) {
                            DocumentsContract.buildDocumentUriUsingTree(parentUri, childDocId)
                        } else {
                            parentUri
                        }

                        result.add(
                            CachedDocumentFile(
                                uri = childUri,
                                name = name ?: "Unknown",
                                isDirectory = isDir,
                                lastModified = lastModified,
                                size = size,
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CachedDocumentFile", "Error querying DocumentsContract for $parentUri, falling back to DocumentFile", e)
                val parentDoc = DocumentFile.fromTreeUri(context, parentUri) ?: DocumentFile.fromSingleUri(context, parentUri)
                if (parentDoc != null && parentDoc.isDirectory) {
                    parentDoc.listFiles().forEach { doc ->
                        result.add(fromDocumentFile(doc))
                    }
                } else if (parentDoc != null) {
                    result.add(fromDocumentFile(parentDoc))
                }
            }
            return result
        }
    }
}
