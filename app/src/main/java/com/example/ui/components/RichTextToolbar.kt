package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RichTextToolbar(
    onApplyFormat: (formatTag: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            IconButton(
                onClick = { onApplyFormat("H1") },
                modifier = Modifier.testTag("format_h1")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Title, contentDescription = "H1", modifier = Modifier.size(20.dp))
                    Text("1", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { onApplyFormat("H2") },
                modifier = Modifier.testTag("format_h2")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Title, contentDescription = "H2", modifier = Modifier.size(18.dp))
                    Text("2", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

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
        }
    }
}
