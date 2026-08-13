package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class BackupWrapper(
    val version: Int = 1,
    val categories: List<CategoryEntity>,
    val notes: List<NoteEntity>
)

class NoteRepository(
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(BackupWrapper::class.java)

    val activeNotes: Flow<List<NoteEntity>> = noteDao.getActiveNotes()
    val favoriteNotes: Flow<List<NoteEntity>> = noteDao.getFavoriteNotes()
    val archivedNotes: Flow<List<NoteEntity>> = noteDao.getArchivedNotes()
    val trashedNotes: Flow<List<NoteEntity>> = noteDao.getTrashedNotes()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getNotesByCategory(categoryId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesByCategory(categoryId)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?> = noteDao.getNoteByIdFlow(id)

    suspend fun saveNote(note: NoteEntity): Long = withContext(Dispatchers.IO) {
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        if (note.id == 0L) {
            noteDao.insertNote(updatedNote)
        } else {
            noteDao.updateNote(updatedNote)
            note.id
        }
    }

    suspend fun togglePin(noteId: Long) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(noteId: Long) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId) ?: return@withContext
        noteDao.updateNote(note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleArchive(noteId: Long) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId) ?: return@withContext
        noteDao.updateNote(
            note.copy(
                isArchived = !note.isArchived,
                isPinned = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun moveToTrash(noteId: Long) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId) ?: return@withContext
        noteDao.updateNote(
            note.copy(
                isTrashed = true,
                trashedTimestamp = System.currentTimeMillis(),
                isPinned = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun restoreFromTrash(noteId: Long) = withContext(Dispatchers.IO) {
        val note = noteDao.getNoteById(noteId) ?: return@withContext
        noteDao.updateNote(
            note.copy(
                isTrashed = false,
                trashedTimestamp = null,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun permanentlyDelete(noteId: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        noteDao.emptyTrash()
    }

    suspend fun duplicateNote(noteId: Long): Long = withContext(Dispatchers.IO) {
        val source = noteDao.getNoteById(noteId) ?: return@withContext 0L
        val duplicate = source.copy(
            id = 0,
            title = "${source.title} (Copy)",
            isPinned = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        noteDao.insertNote(duplicate)
    }

    // Category actions
    suspend fun addCategory(name: String, colorHex: String = "#3F51B5"): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(CategoryEntity(name = name, colorHex = colorHex))
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategoryById(categoryId)
    }

    // Backup & Restore
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val categories = categoryDao.getAllCategoriesList()
        val notes = noteDao.getAllRawNotes()
        val backup = BackupWrapper(version = 1, categories = categories, notes = notes)
        backupAdapter.toJson(backup)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = backupAdapter.fromJson(jsonString) ?: return@withContext false
            // Import categories and map old IDs to new IDs
            val categoryIdMap = mutableMapOf<Long, Long>()
            for (cat in backup.categories) {
                val newId = categoryDao.insertCategory(cat.copy(id = 0))
                if (cat.id != 0L) {
                    categoryIdMap[cat.id] = newId
                }
            }

            // Insert notes with updated category IDs
            for (note in backup.notes) {
                val mappedCatId = note.categoryId?.let { categoryIdMap[it] ?: it }
                noteDao.insertNote(note.copy(id = 0, categoryId = mappedCatId))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
