package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.model.NoteFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    categories: List<CategoryEntity>,
    initialFilter: NoteFilter,
    onDismiss: () -> Unit,
    onApply: (NoteFilter) -> Unit,
    onClear: () -> Unit
) {
    var query by remember { mutableStateOf(initialFilter.query) }
    var selectedCategoryId by remember { mutableStateOf(initialFilter.categoryId) }
    var selectedType by remember { mutableStateOf(initialFilter.type ?: "ALL") }
    var pinnedOnly by remember { mutableStateOf(initialFilter.isPinned == true) }
    var favoriteOnly by remember { mutableStateOf(initialFilter.isFavorite == true) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val noteTypes = listOf("ALL", "TEXT", "RICH", "CHECKLIST", "TABLE")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Search Filters") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search text") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adv_search_text_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "All Categories"
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategoryId = null
                                categoryDropdownExpanded = false
                            }
                        )
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Note Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        noteTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Checkboxes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = pinnedOnly,
                        onCheckedChange = { pinnedOnly = it },
                        modifier = Modifier.testTag("adv_search_pinned_checkbox")
                    )
                    Text("Pinned Notes Only", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = favoriteOnly,
                        onCheckedChange = { favoriteOnly = it },
                        modifier = Modifier.testTag("adv_search_favorite_checkbox")
                    )
                    Text("Favorite Notes Only", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        NoteFilter(
                            query = query,
                            categoryId = selectedCategoryId,
                            type = if (selectedType == "ALL") null else selectedType,
                            isPinned = if (pinnedOnly) true else null,
                            isFavorite = if (favoriteOnly) true else null
                        )
                    )
                },
                modifier = Modifier.testTag("apply_adv_search_button")
            ) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.testTag("clear_adv_search_button")
                ) {
                    Text("Clear")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
