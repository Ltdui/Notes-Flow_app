package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.model.ViewMode
import com.example.ui.components.NoteCard
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionNotesScreen(
    viewModel: MainViewModel,
    sectionRoute: String,
    onBack: () -> Unit,
    onNavigateToEditor: (noteId: Long, type: String) -> Unit
) {
    val favorites by viewModel.favoriteNotes.collectAsState(initial = emptyList())
    val archived by viewModel.archivedNotes.collectAsState(initial = emptyList())
    val trashed by viewModel.trashedNotes.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    val title = when (sectionRoute) {
        "favorites" -> "Favorite Notes"
        "archive" -> "Archived Notes"
        "trash" -> "Trash Bin"
        "categories" -> "Categories"
        else -> "Notes"
    }

    val notes = when (sectionRoute) {
        "favorites" -> favorites
        "archive" -> archived
        "trash" -> trashed
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("section_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (sectionRoute == "trash" && trashed.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.emptyTrash() },
                            modifier = Modifier.testTag("empty_trash_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Empty Trash",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (sectionRoute == "categories") {
            // Categories List Screen
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_card_${cat.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Notes List/Empty State
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.FolderZip,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No $title",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(notes, key = { it.id }) { note ->
                        val cat = categories.find { it.id == note.categoryId }
                        Column {
                            NoteCard(
                                note = note,
                                category = cat,
                                viewMode = ViewMode.LIST,
                                onClick = {
                                    if (sectionRoute != "trash") {
                                        onNavigateToEditor(note.id, note.type)
                                    }
                                },
                                onPinToggle = { viewModel.togglePin(note.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(note.id) }
                            )

                            if (sectionRoute == "trash") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.restoreFromTrash(note.id) },
                                        modifier = Modifier.testTag("restore_note_${note.id}")
                                    ) {
                                        Icon(Icons.Default.Restore, contentDescription = "Restore")
                                        Text("Restore")
                                    }
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Button(
                                        onClick = { viewModel.permanentlyDelete(note.id) },
                                        modifier = Modifier.testTag("delete_permanent_note_${note.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
