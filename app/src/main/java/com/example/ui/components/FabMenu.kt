package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun FabMenu(
    onSelectNoteType: (type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabMenuItem(
                    label = "Text Note",
                    icon = Icons.Default.TextFields,
                    onClick = {
                        expanded = false
                        onSelectNoteType("TEXT")
                    },
                    testTag = "fab_option_text"
                )
                FabMenuItem(
                    label = "Rich Text",
                    icon = Icons.Default.FormatListBulleted,
                    onClick = {
                        expanded = false
                        onSelectNoteType("RICH")
                    },
                    testTag = "fab_option_rich"
                )
                FabMenuItem(
                    label = "Checklist",
                    icon = Icons.Default.CheckCircle,
                    onClick = {
                        expanded = false
                        onSelectNoteType("CHECKLIST")
                    },
                    testTag = "fab_option_checklist"
                )
                FabMenuItem(
                    label = "Table Note",
                    icon = Icons.Default.GridOn,
                    onClick = {
                        expanded = false
                        onSelectNoteType("TABLE")
                    },
                    testTag = "fab_option_table"
                )
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.testTag("main_fab_button")
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close Menu" else "New Note",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
