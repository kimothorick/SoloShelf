package com.kimothorick.soloshelf.util

import android.net.Uri

object FileUtils {
    /**
     * Attempts to extract a human-readable path from a Storage Access Framework URI.
     * Example: content://.../primary%3ADownload%2FBook.epub -> "Internal Storage > Download > Book.epub"
     */
    fun getPrettyPathFromUri(
        uriString: String,
    ): String {
        val uri = Uri.parse(uriString)
        val path = uri.path ?: return uriString

        // Handle external storage provider URIs
        if (uri.authority == "com.android.externalstorage.documents") {
            val docId = path.substringAfterLast("/document/", "")
            if (docId.isNotEmpty()) {
                val parts = docId.split(":")
                val storage = if (parts[0] == "primary") "Internal Storage" else parts[0]
                val folderPath = parts.getOrNull(1) ?: ""
                return if (folderPath.isNotEmpty()) "$storage/$folderPath" else storage
            }
        }

        // Handle download provider URIs
        if (uri.authority == "com.android.providers.downloads.documents") {
            val docId = path.substringAfterLast("/document/", "")
            if (docId.startsWith("raw:")) {
                return docId.substringAfter("raw:")
            }
            return "Downloads > $docId"
        }

        // Fallback: show the last segment (filename)
        return uri.lastPathSegment?.substringAfterLast("/") ?: uriString
    }
}
