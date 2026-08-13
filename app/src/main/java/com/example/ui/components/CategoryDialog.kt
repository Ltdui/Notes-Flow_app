package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity

@Composable
fun CategoryDialog(
    categoryToEdit: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit,
    onDelete: ((categoryId: Long) -> Unit)? = null
) {
    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColorHex by remember { mutableStateOf(categoryToEdit?.colorHex ?: "#3F51B5") }

    val presetColors = listOf(
        "#3F51B5", // Indigo
        "#00897B", // Teal
        "#D81B60", // Pink
        "#FB8C00", // Orange
        "#8E24AA", // Purple
        "#43A047", // Green
        "#1E88E5", // Blue
        "#E53935"  // Red
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (categoryToEdit == null) "New Category" else "Manage Category")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Choose Theme Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetColors.forEach { hex ->
                        val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                        val isSelected = selectedColorHex == hex

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim(), selectedColorHex)
                    }
                },
                enabled = categoryName.isNotBlank(),
                modifier = Modifier.testTag("confirm_category_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (categoryToEdit != null && onDelete != null) {
                    TextButton(
                        onClick = { onDelete(categoryToEdit.id) },
                        modifier = Modifier.testTag("delete_category_button")
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
