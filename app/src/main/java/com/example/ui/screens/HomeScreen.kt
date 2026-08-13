package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.model.NoteFilter
import com.example.data.local.model.SortOrder
import com.example.data.local.model.ViewMode
import com.example.ui.components.AdvancedSearchDialog
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.CategoryDialog
import com.example.ui.components.FabMenu
import com.example.ui.components.NoteCard
import com.example.ui.components.SearchBarRow
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToEditor: (noteId: Long?, type: String) -> Unit,
    onNavigateToSection: (route: String) -> Unit
) {
    val displayedNotes by viewModel.displayedNotes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val advancedFilter by viewModel.advancedFilter.collectAsState()
    val isAdvancedActive by viewModel.isAdvancedSearchActive.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var showAdvancedSearchDialog by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 4..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NoteFlow",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Welcome, ${userProfile.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("All Notes") },
                        selected = true,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.testTag("drawer_nav_all_notes")
                    )

                    NavigationDrawerItem(
                        label = { Text("Categories") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSection("categories")
                        },
                        modifier = Modifier.testTag("drawer_nav_categories")
                    )

                    NavigationDrawerItem(
                        label = { Text("Favorites") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSection("favorites")
                        },
                        modifier = Modifier.testTag("drawer_nav_favorites")
                    )

                    NavigationDrawerItem(
                        label = { Text("Archive") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSection("archive")
                        },
                        modifier = Modifier.testTag("drawer_nav_archive")
                    )

                    NavigationDrawerItem(
                        label = { Text("Trash") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSection("trash")
                        },
                        modifier = Modifier.testTag("drawer_nav_trash")
                    )

                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigateToSection("settings")
                        },
                        modifier = Modifier.testTag("drawer_nav_settings")
                    )
                }
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                FabMenu(
                    onSelectNoteType = { type ->
                        val newNote = viewModel.createNewNote(type = type)
                        onNavigateToEditor(newNote.id, type)
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Top Header Greeting & Search
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "$greeting, ${userProfile.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SearchBarRow(
                        query = searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        viewMode = viewMode,
                        onToggleViewMode = {
                            val next = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                            viewModel.setViewMode(next)
                        },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenAdvancedSearch = { showAdvancedSearchDialog = true }
                    )
                }

                // Category Chips
                CategoryChipsRow(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelect = { viewModel.onCategorySelected(it) },
                    onAddCategoryClick = { showAddCategoryDialog = true },
                    onCategoryLongClick = { categoryToEdit = it }
                )

                // Sub-header: Sort & Active Filter indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAdvancedActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Filtered Results",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(
                                    onClick = { viewModel.clearAdvancedFilter() },
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "${displayedNotes.size} ${if (displayedNotes.size == 1) "note" else "notes"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Sort Selector Dropdown
                    Box {
                        TextButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Text(
                                text = when (sortBy) {
                                    SortOrder.UPDATED_DESC -> "Recently Modified"
                                    SortOrder.UPDATED_ASC -> "Oldest Modified"
                                    SortOrder.CREATED_DESC -> "Recently Created"
                                    SortOrder.CREATED_ASC -> "Oldest Created"
                                    SortOrder.TITLE_ASC -> "Title A-Z"
                                    SortOrder.TITLE_DESC -> "Title Z-A"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort")
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recently Modified") },
                                onClick = {
                                    viewModel.setSortBy(SortOrder.UPDATED_DESC)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recently Created") },
                                onClick = {
                                    viewModel.setSortBy(SortOrder.CREATED_DESC)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title A-Z") },
                                onClick = {
                                    viewModel.setSortBy(SortOrder.TITLE_ASC)
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title Z-A") },
                                onClick = {
                                    viewModel.setSortBy(SortOrder.TITLE_DESC)
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes List or Grid
                if (displayedNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.EventNote,
                                contentDescription = "No Notes",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || isAdvancedActive) "No notes match your search" else "No notes yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || isAdvancedActive) "Try clearing filters or search query" else "Tap + below to create your first note",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    if (viewMode == ViewMode.GRID) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(displayedNotes, key = { it.id }) { note ->
                                val cat = categories.find { it.id == note.categoryId }
                                NoteCard(
                                    note = note,
                                    category = cat,
                                    viewMode = viewMode,
                                    onClick = { onNavigateToEditor(note.id, note.type) },
                                    onPinToggle = { viewModel.togglePin(note.id) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(note.id) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(displayedNotes, key = { it.id }) { note ->
                                val cat = categories.find { it.id == note.categoryId }
                                NoteCard(
                                    note = note,
                                    category = cat,
                                    viewMode = viewMode,
                                    onClick = { onNavigateToEditor(note.id, note.type) },
                                    onPinToggle = { viewModel.togglePin(note.id) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddCategoryDialog) {
        CategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name, colorHex ->
                viewModel.addCategory(name, colorHex)
                showAddCategoryDialog = false
            }
        )
    }

    if (categoryToEdit != null) {
        CategoryDialog(
            categoryToEdit = categoryToEdit,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, colorHex ->
                viewModel.updateCategory(categoryToEdit!!.copy(name = name, colorHex = colorHex))
                categoryToEdit = null
            },
            onDelete = { categoryId ->
                viewModel.deleteCategory(categoryId)
                categoryToEdit = null
            }
        )
    }

    if (showAdvancedSearchDialog) {
        AdvancedSearchDialog(
            categories = categories,
            initialFilter = advancedFilter,
            onDismiss = { showAdvancedSearchDialog = false },
            onApply = { filter ->
                viewModel.applyAdvancedFilter(filter)
                showAdvancedSearchDialog = false
            },
            onClear = {
                viewModel.clearAdvancedFilter()
                showAdvancedSearchDialog = false
            }
        )
    }
}
