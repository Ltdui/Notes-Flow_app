package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [NoteEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noteflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate default categories and sample note on first launch
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.categoryDao(), database.noteDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(categoryDao: CategoryDao, noteDao: NoteDao) {
            val personalId = categoryDao.insertCategory(
                CategoryEntity(name = "Personal", colorHex = "#3F51B5")
            )
            val workId = categoryDao.insertCategory(
                CategoryEntity(name = "Work", colorHex = "#00897B")
            )
            val ideasId = categoryDao.insertCategory(
                CategoryEntity(name = "Ideas", colorHex = "#D81B60")
            )
            val studyId = categoryDao.insertCategory(
                CategoryEntity(name = "Study", colorHex = "#FB8C00")
            )

            // Welcome Note
            noteDao.insertNote(
                NoteEntity(
                    title = "Welcome to NoteFlow! 🚀",
                    content = "NoteFlow is your modern, fast, offline-first notes application.\n\n" +
                            "Features include:\n" +
                            "• Rich text & Checklist notes\n" +
                            "• Table notes with row/column editing\n" +
                            "• Custom categories & instant search\n" +
                            "• PIN/Biometric security & PDF export\n" +
                            "• Offline backup and restore\n\n" +
                            "Your notes stay securely on this device.",
                    type = "TEXT",
                    categoryId = personalId,
                    isPinned = true,
                    isFavorite = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Sample Checklist Note
            val checklistSample = """[
                {"id":"1","text":"Explore NoteFlow features","isCompleted":true},
                {"id":"2","text":"Create your first custom category","isCompleted":false},
                {"id":"3","text":"Try exporting a note to PDF","isCompleted":false},
                {"id":"4","text":"Set up App Lock in Settings","isCompleted":false}
            ]""".trimIndent()

            noteDao.insertNote(
                NoteEntity(
                    title = "Getting Started Checklist",
                    content = "Quick tasks to explore NoteFlow",
                    type = "CHECKLIST",
                    categoryId = ideasId,
                    isPinned = false,
                    isFavorite = true,
                    checklistJson = checklistSample,
                    createdAt = System.currentTimeMillis() - 100000,
                    updatedAt = System.currentTimeMillis() - 100000
                )
            )

            // Sample Table Note
            val tableSample = """{
                "headers":["Project","Status","Priority"],
                "rows":[
                    ["NoteFlow App","Completed","High"],
                    ["Design System","Material You","Medium"],
                    ["Local Backup","Functional","High"]
                ]
            }""".trimIndent()

            noteDao.insertNote(
                NoteEntity(
                    title = "Project Status Table",
                    content = "Overview of active projects",
                    type = "TABLE",
                    categoryId = workId,
                    isPinned = false,
                    tableJson = tableSample,
                    createdAt = System.currentTimeMillis() - 200000,
                    updatedAt = System.currentTimeMillis() - 200000
                )
            )
        }
    }
}
