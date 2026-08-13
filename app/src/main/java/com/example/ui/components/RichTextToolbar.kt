package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RichTextToolbar(
    selectedFontFamily: String,
    onFontFamilySelected: (String) -> Unit,
    onApplyFormat: (formatTag: String) -> Unit,
    onAddChecklist: () -> Unit,
    onAddTable: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontMenuExpanded by remember { mutableStateOf(false) }

    val fonts = listOf(
        "DEFAULT" to "Sans-Serif",
        "SERIF" to "Serif",
        "MONOSPACE" to "Monospace",
        "CURSIVE" to "Cursive",
        "SANS_SERIF" to "Condensed"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font Selector Dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    TextButton(
                        onClick = { fontMenuExpanded = true },
                        modifier = Modifier.testTag("font_family_selector")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FontDownload,
                            contentDescription = "Font Family",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = fonts.find { it.first == selectedFontFamily }?.second ?: "Font",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = fontMenuExpanded,
                    onDismissRequest = { fontMenuExpanded = false }
                ) {
                    fonts.forEach { (fontKey, fontLabel) ->
                        DropdownMenuItem(
                            text = {
                                val ff = when (fontKey) {
                                    "SERIF" -> FontFamily.Serif
                                    "MONOSPACE" -> FontFamily.Monospace
                                    "CURSIVE" -> FontFamily.Cursive
                                    "SANS_SERIF" -> FontFamily.SansSerif
                                    else -> FontFamily.Default
                                }
                                Text(fontLabel, fontFamily = ff, fontWeight = FontWeight.Medium)
                            },
                            onClick = {
                                onFontFamilySelected(fontKey)
                                fontMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Headings & Subheadings
            IconButton(
                onClick = { onApplyFormat("H1") },
                modifier = Modifier.testTag("format_h1")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Title, contentDescription = "Heading 1", modifier = Modifier.size(20.dp))
                    Text("1", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { onApplyFormat("H2") },
                modifier = Modifier.testTag("format_h2")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Title, contentDescription = "Heading 2", modifier = Modifier.size(18.dp))
                    Text("2", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { onApplyFormat("H3") },
                modifier = Modifier.testTag("format_h3")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Title, contentDescription = "Subheading", modifier = Modifier.size(16.dp))
                    Text("3", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Bold, Italic, Underline
            IconButton(
                onClick = { onApplyFormat("BOLD") },
                modifier = Modifier.testTag("format_bold")
            ) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold")
            }

            IconButton(
                onClick = { onApplyFormat("ITALIC") },
                modifier = Modifier.testTag("format_italic")
            ) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
            }

            IconButton(
                onClick = { onApplyFormat("UNDERLINE") },
                modifier = Modifier.testTag("format_underline")
            ) {
                Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline")
            }

            // Lists
            IconButton(
                onClick = { onApplyFormat("BULLET") },
                modifier = Modifier.testTag("format_bullet")
            ) {
                Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List")
            }

            IconButton(
                onClick = { onApplyFormat("NUMBERED") },
                modifier = Modifier.testTag("format_numbered")
            ) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List")
            }

            // Integrated Feature Inserts (Checklist & Table)
            IconButton(
                onClick = onAddChecklist,
                modifier = Modifier.testTag("insert_checklist_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Insert Checklist",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onAddTable,
                modifier = Modifier.testTag("insert_table_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Insert Table",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
