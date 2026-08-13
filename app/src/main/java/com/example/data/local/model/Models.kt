package com.example.data.local.model

import androidx.annotation.Keep

@Keep
data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false
)

@Keep
data class TableData(
    val headers: List<String> = listOf("Header 1", "Header 2", "Header 3"),
    val rows: List<List<String>> = listOf(
        listOf("Row 1, Cell 1", "Row 1, Cell 2", "Row 1, Cell 3"),
        listOf("Row 2, Cell 1", "Row 2, Cell 2", "Row 2, Cell 3")
    )
)

enum class NoteType {
    TEXT,
    RICH,
    CHECKLIST,
    TABLE
}

enum class SortOrder {
    UPDATED_DESC,
    UPDATED_ASC,
    CREATED_DESC,
    CREATED_ASC,
    TITLE_ASC,
    TITLE_DESC
}

enum class ViewMode {
    GRID,
    LIST
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Keep
data class RichStyleSpan(
    val start: Int,
    val end: Int,
    val type: String // "BOLD", "ITALIC", "UNDERLINE", "H1", "H2", "H3", "BULLET", "NUMBERED"
)

@Keep
data class NoteFilter(
    val query: String = "",
    val categoryId: Long? = null,
    val type: String? = null,
    val isPinned: Boolean? = null,
    val isFavorite: Boolean? = null,
    val isArchived: Boolean = false,
    val isTrashed: Boolean = false,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val sortBy: SortOrder = SortOrder.UPDATED_DESC
)

@Keep
data class UserProfile(
    val displayName: String = "NoteFlow User",
    val avatarId: String = "default"
)
