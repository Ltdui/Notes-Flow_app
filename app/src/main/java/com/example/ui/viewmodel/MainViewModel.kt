package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.model.ChecklistItem
import com.example.data.local.model.NoteFilter
import com.example.data.local.model.SortOrder
import com.example.data.local.model.TableData
import com.example.data.local.model.ThemeMode
import com.example.data.local.model.UserProfile
import com.example.data.local.model.ViewMode
import com.example.data.repository.NoteRepository
import com.example.data.repository.SettingsRepository
import com.example.pdf.PdfExporter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val noteRepository = NoteRepository(db.noteDao(), db.categoryDao())
    val settingsRepository = SettingsRepository(application)
    val pdfExporter = PdfExporter(application)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Settings flows
    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
    val dynamicColor: StateFlow<Boolean> = settingsRepository.dynamicColor
    val viewMode: StateFlow<ViewMode> = settingsRepository.viewMode
    val sortBy: StateFlow<SortOrder> = settingsRepository.sortBy
    val appLockEnabled: StateFlow<Boolean> = settingsRepository.appLockEnabled
    val userProfile: StateFlow<UserProfile> = settingsRepository.userProfile
    val isFirstRun: StateFlow<Boolean> = settingsRepository.isFirstRun

    // Lock State
    private val _isLocked = MutableStateFlow(settingsRepository.appLockEnabled.value)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Filter & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _advancedFilter = MutableStateFlow(NoteFilter())
    val advancedFilter: StateFlow<NoteFilter> = _advancedFilter.asStateFlow()

    private val _isAdvancedSearchActive = MutableStateFlow(false)
    val isAdvancedSearchActive: StateFlow<Boolean> = _isAdvancedSearchActive.asStateFlow()

    // Categories
    val categories: StateFlow<List<CategoryEntity>> = noteRepository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Active Displayed Notes (Filtered & Sorted)
    private val _displayedNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val displayedNotes: StateFlow<List<NoteEntity>> = _displayedNotes.asStateFlow()

    val favoriteNotes = noteRepository.favoriteNotes
    val archivedNotes = noteRepository.archivedNotes
    val trashedNotes = noteRepository.trashedNotes

    // Toast messages
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    // Currently Editing Note State
    private val _editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingNote: StateFlow<NoteEntity?> = _editingNote.asStateFlow()

    private val _editingChecklist = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val editingChecklist: StateFlow<List<ChecklistItem>> = _editingChecklist.asStateFlow()

    private val _editingTable = MutableStateFlow(TableData())
    val editingTable: StateFlow<TableData> = _editingTable.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow("Saved")
    val autoSaveStatus: StateFlow<String> = _autoSaveStatus.asStateFlow()

    init {
        // Combine raw active notes with query, category filter, advanced filter, and sort order
        viewModelScope.launch {
            combine(
                noteRepository.activeNotes,
                _searchQuery,
                _selectedCategoryId,
                _advancedFilter,
                _isAdvancedSearchActive,
                sortBy
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val rawNotes = args[0] as List<NoteEntity>
                val query = args[1] as String
                val categoryId = args[2] as Long?
                val filter = args[3] as NoteFilter
                val isAdvanced = args[4] as Boolean
                val sort = args[5] as SortOrder
                filterNotes(rawNotes, query, categoryId, filter, isAdvanced, sort)
            }.collect { filtered ->
                _displayedNotes.value = filtered
            }
        }
    }

    private fun filterNotes(
        notes: List<NoteEntity>,
        query: String,
        categoryId: Long?,
        filter: NoteFilter,
        isAdvanced: Boolean,
        sort: SortOrder
    ): List<NoteEntity> {
        var result = notes

        if (isAdvanced) {
            if (filter.query.isNotBlank()) {
                val q = filter.query.lowercase()
                result = result.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
            }
            if (filter.categoryId != null) {
                result = result.filter { it.categoryId == filter.categoryId }
            }
            if (!filter.type.isNullOrBlank()) {
                result = result.filter { it.type == filter.type }
            }
            if (filter.isPinned == true) {
                result = result.filter { it.isPinned }
            }
            if (filter.isFavorite == true) {
                result = result.filter { it.isFavorite }
            }
            if (filter.dateFrom != null) {
                result = result.filter { it.updatedAt >= filter.dateFrom }
            }
            if (filter.dateTo != null) {
                result = result.filter { it.updatedAt <= filter.dateTo }
            }
        } else {
            if (query.isNotBlank()) {
                val q = query.lowercase()
                result = result.filter {
                    it.title.lowercase().contains(q) ||
                            it.content.lowercase().contains(q) ||
                            (it.checklistJson?.lowercase()?.contains(q) == true) ||
                            (it.tableJson?.lowercase()?.contains(q) == true)
                }
            }
            if (categoryId != null) {
                result = result.filter { it.categoryId == categoryId }
            }
        }

        // Apply Sorting (keeping pinned at top for active list)
        val pinned = result.filter { it.isPinned }
        val unpinned = result.filter { !it.isPinned }

        val sortComparator = when (sort) {
            SortOrder.UPDATED_DESC -> compareByDescending<NoteEntity> { it.updatedAt }
            SortOrder.UPDATED_ASC -> compareBy<NoteEntity> { it.updatedAt }
            SortOrder.CREATED_DESC -> compareByDescending<NoteEntity> { it.createdAt }
            SortOrder.CREATED_ASC -> compareBy<NoteEntity> { it.createdAt }
            SortOrder.TITLE_ASC -> compareBy<NoteEntity> { it.title.lowercase() }
            SortOrder.TITLE_DESC -> compareByDescending<NoteEntity> { it.title.lowercase() }
        }

        return pinned.sortedWith(sortComparator) + unpinned.sortedWith(sortComparator)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _isAdvancedSearchActive.value = false
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun applyAdvancedFilter(filter: NoteFilter) {
        _advancedFilter.value = filter
        _isAdvancedSearchActive.value = true
    }

    fun clearAdvancedFilter() {
        _advancedFilter.value = NoteFilter()
        _isAdvancedSearchActive.value = false
        _searchQuery.value = ""
    }

    // App Lock functions
    fun unlockWithPin(pin: String): Boolean {
        val success = settingsRepository.verifyPin(pin)
        if (success) {
            _isLocked.value = false
        }
        return success
    }

    fun unlockByBiometrics() {
        _isLocked.value = false
    }

    fun lockApp() {
        if (appLockEnabled.value) {
            _isLocked.value = true
        }
    }

    fun setAppLock(enabled: Boolean, pin: String? = null) {
        settingsRepository.setAppLockEnabled(enabled, pin)
        if (!enabled) _isLocked.value = false
        emitMessage(if (enabled) "App Lock enabled" else "App Lock disabled")
    }

    // Note Editor Actions
    fun createNewNote(type: String = "TEXT", categoryId: Long? = selectedCategoryId.value): NoteEntity {
        val newNote = NoteEntity(
            id = 0,
            title = "",
            content = "",
            type = type,
            categoryId = categoryId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _editingNote.value = newNote
        _editingChecklist.value = if (type == "CHECKLIST") listOf(ChecklistItem(text = "")) else emptyList()
        _editingTable.value = TableData()
        _autoSaveStatus.value = "New Note"
        return newNote
    }

    fun loadNoteForEditing(noteId: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            if (note != null) {
                _editingNote.value = note
                if (note.type == "CHECKLIST") {
                    _editingChecklist.value = parseChecklist(note.checklistJson)
                }
                if (note.type == "TABLE") {
                    _editingTable.value = parseTable(note.tableJson) ?: TableData()
                }
                _autoSaveStatus.value = "Saved"
            }
        }
    }

    fun updateEditingTitle(title: String) {
        val current = _editingNote.value ?: return
        _editingNote.value = current.copy(title = title)
        triggerAutoSave()
    }

    fun updateEditingContent(content: String) {
        val current = _editingNote.value ?: return
        _editingNote.value = current.copy(content = content)
        triggerAutoSave()
    }

    fun updateEditingCategory(categoryId: Long?) {
        val current = _editingNote.value ?: return
        _editingNote.value = current.copy(categoryId = categoryId)
        triggerAutoSave()
    }

    fun toggleEditingPin() {
        val current = _editingNote.value ?: return
        _editingNote.value = current.copy(isPinned = !current.isPinned)
        triggerAutoSave()
    }

    fun toggleEditingFavorite() {
        val current = _editingNote.value ?: return
        _editingNote.value = current.copy(isFavorite = !current.isFavorite)
        triggerAutoSave()
    }

    // Checklist editing
    fun addChecklistItem(text: String = "") {
        val updated = _editingChecklist.value.toMutableList()
        updated.add(ChecklistItem(text = text))
        _editingChecklist.value = updated
        serializeChecklistAndAutoSave()
    }

    fun updateChecklistItem(index: Int, text: String, isCompleted: Boolean) {
        val updated = _editingChecklist.value.toMutableList()
        if (index in updated.indices) {
            updated[index] = updated[index].copy(text = text, isCompleted = isCompleted)
            _editingChecklist.value = updated
            serializeChecklistAndAutoSave()
        }
    }

    fun removeChecklistItem(index: Int) {
        val updated = _editingChecklist.value.toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _editingChecklist.value = updated
            serializeChecklistAndAutoSave()
        }
    }

    private fun serializeChecklistAndAutoSave() {
        val current = _editingNote.value ?: return
        val json = serializeChecklist(_editingChecklist.value)
        _editingNote.value = current.copy(checklistJson = json)
        triggerAutoSave()
    }

    // Table Editing
    fun updateTableHeader(colIndex: Int, text: String) {
        val currentTable = _editingTable.value
        val headers = currentTable.headers.toMutableList()
        if (colIndex in headers.indices) {
            headers[colIndex] = text
            val updated = currentTable.copy(headers = headers)
            _editingTable.value = updated
            serializeTableAndAutoSave()
        }
    }

    fun updateTableCell(rowIndex: Int, colIndex: Int, text: String) {
        val currentTable = _editingTable.value
        val rows = currentTable.rows.map { it.toMutableList() }.toMutableList()
        if (rowIndex in rows.indices && colIndex in rows[rowIndex].indices) {
            rows[rowIndex][colIndex] = text
            val updated = currentTable.copy(rows = rows)
            _editingTable.value = updated
            serializeTableAndAutoSave()
        }
    }

    fun addTableRow() {
        val currentTable = _editingTable.value
        val cols = currentTable.headers.size.coerceAtLeast(1)
        val newRow = List(cols) { "" }
        val updatedRows = currentTable.rows + listOf(newRow)
        val updated = currentTable.copy(rows = updatedRows)
        _editingTable.value = updated
        serializeTableAndAutoSave()
    }

    fun deleteTableRow(rowIndex: Int) {
        val currentTable = _editingTable.value
        if (rowIndex in currentTable.rows.indices && currentTable.rows.size > 1) {
            val updatedRows = currentTable.rows.toMutableList().apply { removeAt(rowIndex) }
            val updated = currentTable.copy(rows = updatedRows)
            _editingTable.value = updated
            serializeTableAndAutoSave()
        }
    }

    fun addTableColumn() {
        val currentTable = _editingTable.value
        val colCount = currentTable.headers.size + 1
        val updatedHeaders = currentTable.headers + "Header $colCount"
        val updatedRows = currentTable.rows.map { it + "" }
        val updated = currentTable.copy(headers = updatedHeaders, rows = updatedRows)
        _editingTable.value = updated
        serializeTableAndAutoSave()
    }

    fun deleteTableColumn(colIndex: Int) {
        val currentTable = _editingTable.value
        if (colIndex in currentTable.headers.indices && currentTable.headers.size > 1) {
            val updatedHeaders = currentTable.headers.toMutableList().apply { removeAt(colIndex) }
            val updatedRows = currentTable.rows.map { row ->
                row.toMutableList().apply { if (colIndex in indices) removeAt(colIndex) }
            }
            val updated = currentTable.copy(headers = updatedHeaders, rows = updatedRows)
            _editingTable.value = updated
            serializeTableAndAutoSave()
        }
    }

    private fun serializeTableAndAutoSave() {
        val current = _editingNote.value ?: return
        val json = serializeTable(_editingTable.value)
        _editingNote.value = current.copy(tableJson = json)
        triggerAutoSave()
    }

    private fun triggerAutoSave() {
        val note = _editingNote.value ?: return
        if (note.title.isBlank() && note.content.isBlank() && note.checklistJson.isNullOrBlank() && note.tableJson.isNullOrBlank()) {
            return
        }
        _autoSaveStatus.value = "Saving..."
        viewModelScope.launch {
            val savedId = noteRepository.saveNote(note)
            if (note.id == 0L) {
                _editingNote.value = note.copy(id = savedId)
            }
            _autoSaveStatus.value = "Saved"
        }
    }

    // General note actions
    fun togglePin(noteId: Long) {
        viewModelScope.launch {
            noteRepository.togglePin(noteId)
        }
    }

    fun toggleFavorite(noteId: Long) {
        viewModelScope.launch {
            noteRepository.toggleFavorite(noteId)
        }
    }

    fun toggleArchive(noteId: Long) {
        viewModelScope.launch {
            noteRepository.toggleArchive(noteId)
            emitMessage("Note archive state updated")
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            noteRepository.moveToTrash(noteId)
            emitMessage("Moved to Trash")
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            noteRepository.restoreFromTrash(noteId)
            emitMessage("Note restored")
        }
    }

    fun permanentlyDelete(noteId: Long) {
        viewModelScope.launch {
            noteRepository.permanentlyDelete(noteId)
            emitMessage("Permanently deleted")
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
            emitMessage("Trash emptied")
        }
    }

    fun duplicateNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.duplicateNote(noteId)
            emitMessage("Note duplicated")
        }
    }

    fun exportPdf(noteId: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            val category = note.categoryId?.let { catId -> categories.value.find { c -> c.id == catId } }
            val file = pdfExporter.exportNoteToPdf(note, category)
            if (file != null) {
                pdfExporter.sharePdf(file)
            } else {
                emitMessage("Failed to generate PDF")
            }
        }
    }

    // Category Management
    fun addCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            noteRepository.addCategory(name, colorHex)
            emitMessage("Category '$name' created")
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            noteRepository.updateCategory(category)
            emitMessage("Category updated")
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            noteRepository.deleteCategory(categoryId)
            if (_selectedCategoryId.value == categoryId) {
                _selectedCategoryId.value = null
            }
            emitMessage("Category deleted")
        }
    }

    // Preferences & Settings
    fun setThemeMode(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
    fun setDynamicColor(enabled: Boolean) = settingsRepository.setDynamicColor(enabled)
    fun setViewMode(mode: ViewMode) = settingsRepository.setViewMode(mode)
    fun setSortBy(sortOrder: SortOrder) = settingsRepository.setSortBy(sortOrder)
    fun setUserProfile(displayName: String, avatarId: String) = settingsRepository.setUserProfile(displayName, avatarId)
    fun completeFirstRun() = settingsRepository.completeFirstRun()

    // Backup & Restore
    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val json = noteRepository.exportBackupJson()
            onResult(json)
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val success = noteRepository.importBackupJson(jsonString)
            if (success) {
                emitMessage("Backup restored successfully!")
            } else {
                emitMessage("Failed to restore backup")
            }
        }
    }

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _messageEvent.emit(msg)
        }
    }

    // Moshi Helpers
    private fun parseChecklist(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, ChecklistItem::class.java)
            val adapter = moshi.adapter<List<ChecklistItem>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeChecklist(list: List<ChecklistItem>): String {
        return try {
            val type = Types.newParameterizedType(List::class.java, ChecklistItem::class.java)
            val adapter = moshi.adapter<List<ChecklistItem>>(type)
            adapter.toJson(list)
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun parseTable(json: String?): TableData? {
        if (json.isNullOrBlank()) return null
        return try {
            val adapter = moshi.adapter(TableData::class.java)
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }

    private fun serializeTable(table: TableData): String {
        return try {
            val adapter = moshi.adapter(TableData::class.java)
            adapter.toJson(table)
        } catch (e: Exception) {
            "{}"
        }
    }
}
