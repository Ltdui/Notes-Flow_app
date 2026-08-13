package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.model.ViewMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: NoteEntity,
    category: CategoryEntity?,
    viewMode: ViewMode,
    onClick: () -> Unit,
    onPinToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(note.updatedAt))

    val fallbackColor = MaterialTheme.colorScheme.secondaryContainer
    val categoryColor = try {
        if (category?.colorHex != null) Color(android.graphics.Color.parseColor(category.colorHex))
        else fallbackColor
    } catch (e: Exception) {
        fallbackColor
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}")
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (viewMode == ViewMode.GRID) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header: Title & Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifBlank { "Untitled Note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        IconButton(
                            onClick = onFavoriteToggle,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("favorite_btn_${note.id}")
                        ) {
                            Icon(
                                imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (note.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content Preview
                val previewText = when (note.type) {
                    "CHECKLIST" -> "Checklist item list..."
                    "TABLE" -> "Table note..."
                    else -> note.content.ifBlank { "No additional text" }
                }

                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Footer: Category badge & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (category != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .padding(end = 4.dp)
                                )
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Type Icon
                        val typeIcon = when (note.type) {
                            "CHECKLIST" -> Icons.Default.CheckCircle
                            "TABLE" -> Icons.Default.GridOn
                            "RICH" -> Icons.Default.FormatListBulleted
                            else -> Icons.AutoMirrored.Filled.Label
                        }
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = note.type,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            // LIST VIEW ROW
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = note.title.ifBlank { "Untitled Note" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = note.content.ifBlank { "No content" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (category != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${category.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("favorite_btn_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (note.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
