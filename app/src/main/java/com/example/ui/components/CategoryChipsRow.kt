package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryChipsRow(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long?,
    onCategorySelect: (Long?) -> Unit,
    onAddCategoryClick: () -> Unit,
    onCategoryLongClick: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.testTag("category_chips_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All" Chip
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelect(null) },
                label = { Text("All Notes") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("category_chip_all")
            )
        }

        // Custom Categories
        items(categories, key = { it.id }) { category ->
            val isSelected = selectedCategoryId == category.id
            val categoryColor = try {
                Color(android.graphics.Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.secondary
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .testTag("category_chip_${category.id}")
                    .combinedClickable(
                        onClick = { onCategorySelect(category.id) },
                        onLongClick = { onCategoryLongClick(category) }
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // "+ Add Category" Chip
        item {
            FilterChip(
                selected = false,
                onClick = onAddCategoryClick,
                label = { Text("Add Category") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Category",
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_category_chip")
            )
        }
    }
}
