package com.kimothorick.soloshelf.util

import androidx.compose.ui.unit.dp
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.mediatype.MediaType

object Constants {
    const val DATASTORE_NAME = "SoloshelfSettings"

    val BOOK_ITEM_HEIGHT_COMPACT = 168.dp
    val BOOK_ITEM_HEIGHT_MEDIUM = 216.dp

    val EBOOK_ITEM_WIDTH_COMPACT = 126.dp
    val EBOOK_ITEM_WIDTH_MEDIUM = 162.dp

    val AUDIOBOOK_ITEM_WIDTH_COMPACT = 168.dp
    val AUDIOBOOK_ITEM_WIDTH_MEDIUM = 216.dp

    val SUPPORTED_EXTENSIONS = listOf(
        "EPUB",
        "PDF",
        "LCPDF",
        "LPF",
        "AUDIOBOOK",
        "ZIP",
        "MP3",
        "AAC",
        "M4A",
        "M4B",
        "OGG",
        "WAV",
        "FLAC",
        "OPUS",
    )

    val EPUB_EXTENSIONS = listOf("EPUB", "ZIP")
    val PDF_EXTENSIONS = listOf("PDF", "LCPDF")
    val AUDIO_EXTENSIONS = listOf(
        "AUDIOBOOK",
        "LPF",
        "MP3",
        "AAC",
        "M4A",
        "M4B",
        "OGG",
        "WAV",
        "FLAC",
        "OPUS",
    )
    val SOLOSHELF_GITHUB_URL = "https://github.com/kimothorick/SoloShelf"
}

val Format.isAudiobook: Boolean
    get() = mediaType.isAudio || 
            mediaType.matchesAny(MediaType.READIUM_AUDIOBOOK, MediaType.READIUM_AUDIOBOOK_MANIFEST, MediaType.LPF) ||
            fileExtension.value.uppercase() in Constants.AUDIO_EXTENSIONS

val Format.isEbook: Boolean
    get() = mediaType.matchesAny(MediaType.EPUB, MediaType.PDF, MediaType.LCP_PROTECTED_PDF) ||
            fileExtension.value.uppercase() in Constants.EPUB_EXTENSIONS

fun Format.isSupported(): Boolean =
    isAudiobook || isEbook || fileExtension.value.uppercase() in Constants.SUPPORTED_EXTENSIONS
