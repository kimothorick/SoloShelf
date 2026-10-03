package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class CoverScanner(private val context: Context) {

    fun scanCover(directoryUri: Uri, chapters: List<ChapterRecord>): String? {
        try {
            val cacheDir = File(context.cacheDir, "soloshelf_covers").apply { if (!exists()) mkdirs() }
            val coverFileName = "cover_${directoryUri.toString().hashCode()}.jpg"
            val existingFile = File(cacheDir, coverFileName)
            if (existingFile.exists() && existingFile.length() > 0) {
                return existingFile.absolutePath
            }

            try {
                val imageFiles = CachedDocumentFile.listDirectoryFiles(context, directoryUri)
                    .filter { CachedDocumentFile.isImageExtension(it.extension) }

                val preferredNames = listOf("cover", "folder", "front", "album", "artwork")
                val matchedFile = imageFiles.firstOrNull { img ->
                    val clean = img.name.substringBeforeLast('.').lowercase()
                    preferredNames.any { pref -> clean.contains(pref) }
                } ?: imageFiles.firstOrNull()

                if (matchedFile != null) {
                    try {
                        context.contentResolver.openInputStream(matchedFile.uri)?.use { stream ->
                            val bitmap = decodeSampledBitmap(stream, 512, 512)
                            if (bitmap != null) {
                                return saveThumbnail(bitmap, coverFileName)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CoverScanner", "Error reading image file ${matchedFile.uri}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("CoverScanner", "Error listing image files in directory $directoryUri", e)
            }

            for (chapter in chapters) {
                try {
                    val embedded = extractEmbeddedCover(chapter.uri)
                    if (embedded != null) {
                        return saveThumbnail(embedded, coverFileName)
                    }
                } catch (e: Exception) {
                    Log.e("CoverScanner", "Error extracting embedded cover from ${chapter.uri}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CoverScanner", "Fatal error in scanCover for $directoryUri", e)
        }
        return null
    }

    fun scanSynopsis(directoryUri: Uri): String? {
        try {
            val files = CachedDocumentFile.listDirectoryFiles(context, directoryUri)
            val synopsisFile = files.firstOrNull { file ->
                val name = file.name.lowercase()
                name == "description.txt" || name == "summary.txt" || name == "synopsis.txt" || name == "info.txt"
            }
            if (synopsisFile != null) {
                context.contentResolver.openInputStream(synopsisFile.uri)?.bufferedReader()?.use { reader ->
                    return reader.readText().trim().takeIf { it.isNotBlank() }
                }
            }
        } catch (e: Exception) {
            Log.e("CoverScanner", "Error scanning synopsis file", e)
        }
        return null
    }

    private fun decodeSampledBitmap(inputStream: java.io.InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        val bytes = inputStream.readBytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun extractEmbeddedCover(uri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture?.let { bytes ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                options.inSampleSize = calculateInSampleSize(options, 512, 512)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            }
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun saveThumbnail(bitmap: Bitmap, filename: String): String? {
        return try {
            val cacheDir = File(context.cacheDir, "soloshelf_covers").apply { if (!exists()) mkdirs() }
            val targetFile = File(cacheDir, filename)
            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e("CoverScanner", "Error saving cover thumbnail $filename", e)
            null
        }
    }
}
