package com.kimothorick.soloshelf.domain

enum class ImportStatus {
    QUEUED,
    IN_PROGRESS,
    ADDED,
    ALREADY_EXISTS,
    FAILED,
}

data class ImportTaskItem(
    val id: String,
    val fileName: String,
    val fileSize: String,
    val status: ImportStatus,
    val progress: Float = 0f,
    val subtitle: String = "Ready",
)

data class ImportState(
    val isImporting: Boolean = false,
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val items: List<ImportTaskItem> = emptyList(),
)
