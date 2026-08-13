package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CategoryDialog
import com.example.ui.components.ChecklistEditor
import com.example.ui.components.RichTextToolbar
import com.example.ui.components.TableEditorGrid
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: MainViewModel,
    noteId: Long?,
    type: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val editingNote by viewModel.editingNote.collectAsState()
    val checklistItems by viewModel.editingChecklist.collectAsState()
    val tableData by viewModel.editingTable.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(noteId, type) {
        if (noteId != null && noteId != 0L) {
            viewModel.loadNoteForEditing(noteId)
        } else {
            viewModel.createNewNote(type = type)
        }
    }

    val note = editingNote ?: return

    val currentCategory = categories.find { it.id == note.categoryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = autoSaveStatus,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Pin Button
                    IconButton(
                        onClick = { viewModel.toggleEditingPin() },
                        modifier = Modifier.testTag("editor_pin_button")
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleEditingFavorite() },
                        modifier = Modifier.testTag("editor_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite Note",
                            tint = if (note.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("editor_more_menu_button")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Share Note") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
                                        setType("text/plain")
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Note"))
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Export to PDF") },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    if (note.id != 0L) {
                                        viewModel.exportPdf(note.id)
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    if (note.id != 0L) {
                                        viewModel.duplicateNote(note.id)
                                        onBack()
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Archive") },
                                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    if (note.id != 0L) {
                                        viewModel.toggleArchive(note.id)
                                        onBack()
                                    }
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Move to Trash") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    if (note.id != 0L) {
                                        viewModel.moveToTrash(note.id)
                                        onBack()
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (note.type == "TEXT" || note.type == "RICH_TEXT") {
                RichTextToolbar(
                    onApplyFormat = { format ->
                        when (format) {
                            "BOLD" -> viewModel.updateEditingContent("${note.content} **bold text**")
                            "ITALIC" -> viewModel.updateEditingContent("${note.content} *italic text*")
                            "UNDERLINE" -> viewModel.updateEditingContent("${note.content} <u>underlined</u>")
                            "H1" -> viewModel.updateEditingContent("${note.content}\n# Heading 1\n")
                            "H2" -> viewModel.updateEditingContent("${note.content}\n## Heading 2\n")
                            "BULLET" -> viewModel.updateEditingContent("${note.content}\n- Bullet point\n")
                            "NUMBERED" -> viewModel.updateEditingContent("${note.content}\n1. First item\n")
                            else -> {}
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Category Chip Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = currentCategory?.let {
                        try {
                            Color(android.graphics.Color.parseColor(it.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    } ?: MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { showCategoryPicker = true }
                        .testTag("editor_category_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentCategory?.name ?: "Select Category",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (currentCategory != null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = showCategoryPicker,
                    onDismissRequest = { showCategoryPicker = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            viewModel.updateEditingCategory(null)
                            showCategoryPicker = false
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val catColor = try {
                                        Color(android.graphics.Color.parseColor(category.colorHex))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(catColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(category.name)
                                }
                            },
                            onClick = {
                                viewModel.updateEditingCategory(category.id)
                                showCategoryPicker = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("+ New Category", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showCategoryPicker = false
                            showAddCategoryDialog = true
                        }
                    )
                }
            }

            // Title Field
            OutlinedTextField(
                value = note.title,
                onValueChange = { viewModel.updateEditingTitle(it) },
                placeholder = { Text("Note Title", style = MaterialTheme.typography.headlineMedium) },
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_title_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body based on Type
            when (note.type) {
                "CHECKLIST" -> {
                    ChecklistEditor(
                        items = checklistItems,
                        onItemChange = { idx, text, completed ->
                            viewModel.updateChecklistItem(idx, text, completed)
                        },
                        onAddItem = { viewModel.addChecklistItem() },
                        onRemoveItem = { idx -> viewModel.removeChecklistItem(idx) }
                    )
                }
                "TABLE" -> {
                    TableEditorGrid(
                        tableData = tableData,
                        onHeaderChange = { colIdx, text -> viewModel.updateTableHeader(colIdx, text) },
                        onCellChange = { rowIdx, colIdx, text -> viewModel.updateTableCell(rowIdx, colIdx, text) },
                        onAddRow = { viewModel.addTableRow() },
                        onDeleteRow = { rowIdx -> viewModel.deleteTableRow(rowIdx) },
                        onAddColumn = { viewModel.addTableColumn() },
                        onDeleteColumn = { colIdx -> viewModel.deleteTableColumn(colIdx) }
                    )
                }
                else -> { // TEXT or RICH_TEXT
                    OutlinedTextField(
                        value = note.content,
                        onValueChange = { viewModel.updateEditingContent(it) },
                        placeholder = { Text("Start typing your note...") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .testTag("editor_content_input")
                    )
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        CategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, colorHex ->
                viewModel.addCategory(name, colorHex)
                showAddCategoryDialog = false
            }
        )
    }
}
