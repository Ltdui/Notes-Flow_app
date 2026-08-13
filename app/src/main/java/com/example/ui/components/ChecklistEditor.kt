package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.model.ChecklistItem

@Composable
fun ChecklistEditor(
    items: List<ChecklistItem>,
    onItemChange: (index: Int, text: String, isCompleted: Boolean) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = items.count { it.isCompleted }
    val totalCount = items.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        // Progress Header
        if (totalCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount of $totalCount completed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // List of items
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { checked ->
                        onItemChange(index, item.text, checked)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("checklist_item_checkbox_$index")
                )

                TextField(
                    value = item.text,
                    onValueChange = { text ->
                        onItemChange(index, text, item.isCompleted)
                    },
                    placeholder = { Text("Task item...") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (item.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("checklist_item_text_$index")
                )

                IconButton(
                    onClick = { onRemoveItem(index) },
                    modifier = Modifier.testTag("checklist_item_remove_$index")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onAddItem,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("add_checklist_item_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add item")
        }
    }
}
