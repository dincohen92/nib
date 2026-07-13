package com.example.bullet.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bullet.data.db.Feeling
import com.example.bullet.data.db.JournalEntry
import com.example.bullet.data.db.toFeelingSet
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val shortDateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
private val longDateFormatter  = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)
private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun JournalScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<JournalEntry?>(null) }
    var viewingEntry by remember { mutableStateOf<JournalEntry?>(null) }

    val grouped = remember(entries) {
        entries
            .sortedByDescending { it.date }
            .groupBy { YearMonth.parse(it.date.substring(0, 7)) }
            .entries
            .sortedByDescending { it.key }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                            onView  = { viewingEntry = entry },
                            onEdit  = { editingEntry = entry },
                            onDelete = { viewModel.deleteEntry(entry) },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "New entry")
        }
    }

    if (showAddDialog) {
        JournalEntryDialog(
            initial = "",
            initialFeelings = emptySet(),
            title = "New entry",
            onDismiss = { showAddDialog = false },
            onConfirm = { content, feelings ->
                viewModel.addEntry(content, LocalDate.now().format(isoFormatter), feelings)
                showAddDialog = false
            },
        )
    }

    editingEntry?.let { entry ->
        JournalEntryDialog(
            initial = entry.content,
            initialFeelings = entry.feelings.toFeelingSet(),
            title = "Edit entry",
            onDismiss = { editingEntry = null },
            onConfirm = { content, feelings ->
                viewModel.updateEntry(entry, content, feelings)
                editingEntry = null
            },
        )
    }

    viewingEntry?.let { entry ->
        JournalEntryViewSheet(
            entry = entry,
            onDismiss = { viewingEntry = null },
            onEdit = { viewingEntry = null; editingEntry = entry },
            onDelete = { viewModel.deleteEntry(entry); viewingEntry = null },
        )
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    onView: () -> Unit,
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
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onView),
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
            Spacer(Modifier.size(12.dp))
        }
        val feelings = remember(entry.feelings) { entry.feelings.toFeelingSet() }
        Row(
            modifier = Modifier.padding(start = 24.dp, end = 48.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.content,
                fontSize = 13.sp,
                letterSpacing = 0.1.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (feelings.isNotEmpty()) {
                Text(
                    text = feelings.joinToString("") { it.emoji },
                    fontSize = 12.sp,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalEntryViewSheet(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val date = remember(entry.date) {
        runCatching { LocalDate.parse(entry.date, isoFormatter).format(longDateFormatter) }
            .getOrDefault(entry.date)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val feelings = remember(entry.feelings) { entry.feelings.toFeelingSet() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (feelings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    feelings.forEach { feeling ->
                        Text(
                            text = "${feeling.emoji} ${feeling.label}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = { onDelete() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
                TextButton(
                    onClick = { onEdit() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Edit")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalEntryDialog(
    initial: String,
    initialFeelings: Set<Feeling>,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Set<Feeling>) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    var selectedFeelings by remember { mutableStateOf(initialFeelings) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("What's on your mind?") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Feeling.entries.forEach { feeling ->
                        val selected = feeling in selectedFeelings
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedFeelings = if (selected)
                                    selectedFeelings - feeling
                                else
                                    selectedFeelings + feeling
                            },
                            label = { Text("${feeling.emoji} ${feeling.label}") },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text, selectedFeelings) },
                enabled = text.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
