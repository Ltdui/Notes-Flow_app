package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isTrashed = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getActiveNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND isTrashed = 0 ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getActiveNotesList(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getFavoriteNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isTrashed = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY trashedTimestamp DESC")
    fun getTrashedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId AND isArchived = 0 AND isTrashed = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByCategory(categoryId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteByIdFlow(id: Long): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("DELETE FROM notes WHERE isTrashed = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM notes")
    suspend fun getAllRawNotes(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
