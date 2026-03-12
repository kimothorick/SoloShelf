package com.kimothorick.soloshelf.util

object Constants {
    const val DATASTORE_NAME = "SoloshelfSettings"

    val SUPPORTED_EXTENSIONS = listOf(
        "EPUB",
        "PDF",
        "AUDIOBOOK",
        "MP3",
        "AAC",
        "M4A",
        "M4B",
        "OGG",
        "WAV"
    )
    
    val EPUB_EXTENSIONS = listOf("EPUB")
    val PDF_EXTENSIONS = listOf("PDF")
    val AUDIO_EXTENSIONS = listOf("AUDIOBOOK", "MP3", "AAC", "M4A", "M4B", "OGG", "WAV")
}
