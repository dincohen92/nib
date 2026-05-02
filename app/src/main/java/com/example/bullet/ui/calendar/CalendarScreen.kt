package com.example.bullet.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bullet.data.db.JournalEntry
import com.example.bullet.data.db.Task
import com.example.bullet.ui.shared.AddEntrySheet
import com.example.bullet.ui.shared.TaskActionSheet
import com.example.bullet.ui.shared.TaskRow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun CalendarScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val countsByDate by viewModel.taskCountsByDate.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntriesForSelectedDate.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var swipeDeltaX by remember { mutableFloatStateOf(0f) }
    var showJournalDialog by remember { mutableStateOf(false) }
    var editingJournalEntry by remember { mutableStateOf<JournalEntry?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeDeltaX > 150f) viewModel.cycleViewMode(forward = false)
                        else if (swipeDeltaX < -150f) viewModel.cycleViewMode(forward = true)
                        swipeDeltaX = 0f
                    },
                    onDragCancel = { swipeDeltaX = 0f },
                    onHorizontalDrag = { _, delta -> swipeDeltaX += delta },
                )
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CalendarHeader(
                selectedDate = selectedDate,
                viewMode = viewMode,
                onPrev = { viewModel.navigatePeriod(false) },
                onNext = { viewModel.navigatePeriod(true) },
                onToday = { viewModel.goToToday() },
                onSettings = onSettingsClick,
            )

            CalendarTabStrip(
                viewMode = viewMode,
                onSelect = { viewModel.setViewMode(it) },
            )

            if (viewMode == ViewMode.WEEK) {
                WeekGrid(
                    selectedDate = selectedDate,
                    countsByDate = countsByDate,
                    onSelectDate = { viewModel.selectDate(it) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
            } else if (viewMode == ViewMode.MONTH) {
                MonthGrid(
                    selectedDate = selectedDate,
                    countsByDate = countsByDate,
                    onSelectDate = { viewModel.selectDate(it) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Tasks section header
                item(key = "tasks-header") {
                    SectionLabel("Tasks")
                }

                if (tasks.isEmpty()) {
                    item(key = "tasks-empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                        ) {
                            Text(
                                "No tasks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(tasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onBulletClick = { viewModel.cycleStatus(task) },
                            onLongClick = { selectedTask = task },
                        )
                    }
                }

                // Journal section header
                item(key = "journal-header") {
                    SectionLabel("Journal")
                }

                if (journalEntries.isEmpty()) {
                    item(key = "journal-cta") {
                        NewJournalEntryRow(onClick = { showJournalDialog = true })
                    }
                } else {
                    items(journalEntries, key = { "j-${it.id}" }) { entry ->
                        CalendarJournalRow(
                            entry = entry,
                            onEdit = { editingJournalEntry = entry },
                            onDelete = { viewModel.deleteJournalEntry(entry) },
                        )
                    }
                    item(key = "journal-add") {
                        NewJournalEntryRow(onClick = { showJournalDialog = true })
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add task")
        }
    }

    if (showAddSheet) {
        AddEntrySheet(
            defaultDate = selectedDate,
            onDismiss = { showAddSheet = false },
            onAdd = { content, date, priority ->
                viewModel.addTask(content, date, priority)
                showAddSheet = false
            },
        )
    }

    selectedTask?.let { task ->
        TaskActionSheet(
            task = task,
            onDismiss = { selectedTask = null },
            onSave = { content, date, priority -> viewModel.saveTaskEdits(task, content, date, priority) },
            onDelete = { viewModel.deleteTask(task) },
        )
    }

    if (showJournalDialog) {
        JournalEntryDialog(
            initial = "",
            title = "New journal entry",
            onDismiss = { showJournalDialog = false },
            onConfirm = { content ->
                viewModel.addJournalEntry(content)
                showJournalDialog = false
            },
        )
    }

    editingJournalEntry?.let { entry ->
        JournalEntryDialog(
            initial = entry.content,
            title = "Edit entry",
            onDismiss = { editingJournalEntry = null },
            onConfirm = { content ->
                viewModel.updateJournalEntry(entry, content)
                editingJournalEntry = null
            },
        )
    }
}

@Composable
private fun SectionLabel(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}

@Composable
private fun NewJournalEntryRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = "New journal entry…",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f),
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun CalendarJournalRow(
    entry: JournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = entry.content,
                fontSize = 13.sp,
                letterSpacing = 0.1.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(8.dp))
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
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
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

@Composable
private fun CalendarHeader(
    selectedDate: LocalDate,
    viewMode: ViewMode,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSettings: () -> Unit = {},
) {
    val today = LocalDate.now()
    val isToday = selectedDate == today

    val title = when (viewMode) {
        ViewMode.DAY -> when (selectedDate) {
            today -> "Today · ${today.format(DateTimeFormatter.ofPattern("MMM d"))}"
            today.minusDays(1) -> "Yesterday · ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
            today.plusDays(1) -> "Tomorrow · ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        }
        ViewMode.WEEK -> {
            val mon = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val sun = mon.plusDays(6)
            if (mon.month == sun.month) {
                mon.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            } else {
                "${mon.format(DateTimeFormatter.ofPattern("MMM"))} – ${sun.format(DateTimeFormatter.ofPattern("MMM yyyy"))}"
            }
        }
        ViewMode.MONTH -> selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onToday) {
            Text(
                "Today",
                color = if (isToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else MaterialTheme.colorScheme.primary,
            )
        }
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CalendarTabStrip(
    viewMode: ViewMode,
    onSelect: (ViewMode) -> Unit,
) {
    val tabs = ViewMode.entries
    val selectedIndex = tabs.indexOf(viewMode)

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, mode ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSelect(mode) },
                text = {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}

// ── Day circle ───────────────────────────────────────────────────────────────

@Composable
private fun DayCircle(
    date: LocalDate,
    isSelected: Boolean,
    counts: TaskCounts?,
    dimmed: Boolean = false,
    showDayLetter: Boolean = false,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val today = LocalDate.now()
    val isToday = date == today
    val green = MaterialTheme.colorScheme.primary
    val circleSize = if (compact) 28.dp else 36.dp
    val fontSize = if (compact) 11.sp else 14.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        if (showDayLetter) {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = if (dimmed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        val onGreen = MaterialTheme.colorScheme.onPrimary
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(circleSize)
                .then(
                    when {
                        isToday && isSelected -> Modifier
                            .background(green, CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        isToday -> Modifier.background(green, CircleShape)
                        isSelected -> Modifier
                            .clip(CircleShape)
                            .border(2.dp, green, CircleShape)
                        else -> Modifier
                    }
                ),
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = fontSize,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> onGreen
                    dimmed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }

        if (counts != null && (counts.open > 0 || counts.closed > 0)) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (counts.open > 0) {
                    Text(
                        text = counts.open.toString(),
                        fontSize = if (compact) 8.sp else 10.sp,
                        color = green,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (counts.open > 0 && counts.closed > 0) {
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (counts.closed > 0) {
                    Text(
                        text = counts.closed.toString(),
                        fontSize = if (compact) 8.sp else 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
            }
        }
    }
}

// ── Week grid ────────────────────────────────────────────────────────────────

@Composable
private fun WeekGrid(
    selectedDate: LocalDate,
    countsByDate: Map<LocalDate, TaskCounts>,
    onSelectDate: (LocalDate) -> Unit,
) {
    val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        weekDays.forEach { day ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DayCircle(
                    date = day,
                    isSelected = day == selectedDate,
                    counts = countsByDate[day],
                    showDayLetter = true,
                    onClick = { onSelectDate(day) },
                )
            }
        }
    }
}

// ── Month grid ───────────────────────────────────────────────────────────────

@Composable
private fun MonthGrid(
    selectedDate: LocalDate,
    countsByDate: Map<LocalDate, TaskCounts>,
    onSelectDate: (LocalDate) -> Unit,
) {
    val firstOfMonth = selectedDate.withDayOfMonth(1)
    val lastOfMonth = firstOfMonth.with(TemporalAdjusters.lastDayOfMonth())

    val gridStart = firstOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val gridEnd = lastOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    val allDays = generateSequence(gridStart) { it.plusDays(1) }
        .takeWhile { !it.isAfter(gridEnd) }
        .toList()

    val weeks = allDays.chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val headers = listOf("M", "T", "W", "T", "F", "S", "S")
            headers.forEach { letter ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = letter,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                week.forEach { day ->
                    val outOfMonth = day.month != selectedDate.month
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DayCircle(
                            date = day,
                            isSelected = day == selectedDate,
                            counts = countsByDate[day],
                            dimmed = outOfMonth,
                            compact = true,
                            onClick = { onSelectDate(day) },
                        )
                    }
                }
            }
        }
    }
}
