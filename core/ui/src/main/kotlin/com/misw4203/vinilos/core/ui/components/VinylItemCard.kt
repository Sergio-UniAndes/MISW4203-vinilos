package com.misw4203.vinilos.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VinylItemCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (canEdit || canDelete) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canEdit && onEdit != null) {
                        Button(onClick = onEdit) { Text("Edit") }
                    }
                    if (canDelete && onDelete != null) {
                        Button(onClick = onDelete) { Text("Delete") }
                    }
                }
            }
        }
    }
}

