package com.kimothorick.soloshelf.domain.models

import org.readium.r2.shared.util.mediatype.MediaType

sealed interface LibraryItem {
    val id: Long
    val title: String
    val author: String
    val coverPath: String?
    val filePath: String
    val mediaType: MediaType?
    val lastReadLocatorJson: String?
    val lastAccessed: Long?
    val progressPercentage: Float

    data class Ebook(
        override val id: Long,
        override val title: String,
        override val author: String,
        override val coverPath: String?,
        override val filePath: String,
        override val mediaType: MediaType?,
        override val lastReadLocatorJson: String?,
        override val lastAccessed: Long?,
        override val progressPercentage: Float,
        val pageCount: Int
    ) : LibraryItem
}
