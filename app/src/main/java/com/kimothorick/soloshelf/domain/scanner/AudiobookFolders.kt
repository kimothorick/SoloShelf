package com.kimothorick.soloshelf.domain.scanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

enum class FolderStructureType {
    SINGLE_FILE,
    SINGLE_FOLDER,
    ROOT_FOLDER,
    AUTHOR_DIRECTORY
}

class AudiobookFolders(private val context: Context) {

    fun takePersistablePermission(uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            Log.e("AudiobookFolders", "Failed to take persistable URI permission for $uri", e)
        }
    }

    fun isUriAccessible(uri: Uri): Boolean {
        return try {
            val persistedPermissions = context.contentResolver.persistedUriPermissions
            val hasPermission = persistedPermissions.any { it.uri == uri && it.isReadPermission }
            if (!hasPermission) {
                context.contentResolver.openInputStream(uri)?.use { }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun detectFolderStructure(uri: Uri): FolderStructureType {
        val files = CachedDocumentFile.listFiles(context, uri)
        if (files.isEmpty()) {
            return FolderStructureType.SINGLE_FILE
        }

        val hasAudio = files.any { !it.isDirectory && CachedDocumentFile.isAudioExtension(it.extension) }
        val subDirs = files.filter { it.isDirectory }

        if (hasAudio && subDirs.isEmpty()) {
            return FolderStructureType.SINGLE_FOLDER
        }

        if (subDirs.isNotEmpty()) {
            val firstSubDir = subDirs.first()
            val subFiles = CachedDocumentFile.listFiles(context, firstSubDir.uri)
            val subHasAudio = subFiles.any { !it.isDirectory && CachedDocumentFile.isAudioExtension(it.extension) }
            val subHasDirs = subFiles.any { it.isDirectory }

            if (subHasAudio) {
                return FolderStructureType.ROOT_FOLDER
            } else if (subHasDirs) {
                return FolderStructureType.AUTHOR_DIRECTORY
            } else {
                return FolderStructureType.ROOT_FOLDER
            }
        }

        return FolderStructureType.SINGLE_FILE
    }

    fun resolveBookFolderUris(rootUri: Uri): List<Uri> {
        takePersistablePermission(rootUri)
        val structure = detectFolderStructure(rootUri)
        val bookUris = mutableListOf<Uri>()

        when (structure) {
            FolderStructureType.SINGLE_FILE, FolderStructureType.SINGLE_FOLDER -> {
                bookUris.add(rootUri)
            }
            FolderStructureType.ROOT_FOLDER -> {
                val files = CachedDocumentFile.listFiles(context, rootUri)
                files.filter { it.isDirectory }.forEach { bookDir ->
                    takePersistablePermission(bookDir.uri)
                    bookUris.add(bookDir.uri)
                }
            }
            FolderStructureType.AUTHOR_DIRECTORY -> {
                val authorDirs = CachedDocumentFile.listFiles(context, rootUri).filter { it.isDirectory }
                authorDirs.forEach { authorDir ->
                    val bookDirs = CachedDocumentFile.listFiles(context, authorDir.uri).filter { it.isDirectory }
                    bookDirs.forEach { bookDir ->
                        takePersistablePermission(bookDir.uri)
                        bookUris.add(bookDir.uri)
                    }
                }
            }
        }
        return bookUris
    }
}
