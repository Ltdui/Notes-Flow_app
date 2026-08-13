package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.model.TableData

@Composable
fun TableEditorGrid(
    tableData: TableData,
    onHeaderChange: (colIndex: Int, text: String) -> Unit,
    onCellChange: (rowIndex: Int, colIndex: Int, text: String) -> Unit,
    onAddRow: () -> Unit,
    onDeleteRow: (rowIndex: Int) -> Unit,
    onAddColumn: () -> Unit,
    onDeleteColumn: (colIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Toolbar for Table actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onAddRow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("table_add_row_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Row")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Row")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onAddColumn,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("table_add_col_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Column")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Col")
            }
        }

        // Horizontal Scroll Table Grid
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(12.dp)
            ) {
                // Headers Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    tableData.headers.forEachIndexed { colIndex, header ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = header,
                                onValueChange = { text -> onHeaderChange(colIndex, text) },
                                label = { Text("Header ${colIndex + 1}") },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("table_header_$colIndex")
                            )

                            if (tableData.headers.size > 1) {
                                IconButton(
                                    onClick = { onDeleteColumn(colIndex) },
                                    modifier = Modifier.testTag("table_delete_col_$colIndex")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Column",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rows
                tableData.rows.forEachIndexed { rowIndex, row ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        row.forEachIndexed { colIndex, cellText ->
                            OutlinedTextField(
                                value = cellText,
                                onValueChange = { text -> onCellChange(rowIndex, colIndex, text) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("table_cell_${rowIndex}_$colIndex")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (tableData.rows.size > 1) {
                            IconButton(
                                onClick = { onDeleteRow(rowIndex) },
                                modifier = Modifier.testTag("table_delete_row_$rowIndex")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Row",
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
