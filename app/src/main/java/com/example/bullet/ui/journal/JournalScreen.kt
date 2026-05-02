package com.example.bullet.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bullet.data.db.JournalEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<JournalEntry?>(null) }

    val grouped = remember(entries) {
        entries
            .sortedByDescending { it.date }
            .groupBy { YearMonth.parse(it.date.substring(0, 7)) }
            .entries
            .sortedByDescending { it.key }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Page header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Text(
                text = "Journal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderIconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = "New entry", modifier = Modifier.size(20.dp))
                }
            }
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No entries yet.\nTap + to write your first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            grouped.forEach { (month, monthEntries) ->
                // Month group label
                item(key = "month-$month") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = month.format(monthYearFormatter).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.padding(start = 24.dp),
                        )
                    }
                }

                items(monthEntries, key = { it.id }) { entry ->
                    JournalEntryRow(
                        entry = entry,
                        onEdit = { editingEntry = entry },
                        onDelete = { viewModel.deleteEntry(entry) },
                    )
                }
            }

            // "New entry…" CTA at the bottom
            item(key = "cta") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "New entry…",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }

    if (showAddDialog) {
        JournalEntryDialog(
            initial = "",
            title = "New entry",
            onDismiss = { showAddDialog = false },
            onConfirm = { content ->
                viewModel.addEntry(content, LocalDate.now().format(isoFormatter))
                showAddDialog = false
            },
        )
    }

    editingEntry?.let { entry ->
        JournalEntryDialog(
            initial = entry.content,
            title = "Edit entry",
            onDismiss = { editingEntry = null },
            onConfirm = { content ->
                viewModel.updateEntry(entry, content)
                editingEntry = null
            },
        )
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val date = remember(entry.date) {
        runCatching { LocalDate.parse(entry.date, isoFormatter).format(shortDateFormatter) }
            .getOrDefault(entry.date)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = date,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Edit",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onEdit),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            Spacer(Modifier.size(8.dp))
            Icon(
                Icons.Outlined.Delete,
                contentDescription = "Delete",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onDelete),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
        Text(
            text = entry.content,
            fontSize = 13.sp,
            letterSpacing = 0.1.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 24.dp, end = 48.dp, top = 4.dp),
        )
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun JournalEntryDialog(
    initial: String,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("What's on your mind?") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                enabled = text.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
