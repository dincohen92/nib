package com.example.bullet.ui.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bullet.data.db.Frequency
import com.example.bullet.data.db.RecurringTask
import com.example.bullet.data.db.scheduleDescription
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi

private enum class MonthlyMode { OnDay, OnThe }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecurringScreen(viewModel: RecurringViewModel = hiltViewModel()) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringTask?>(null) }
    var actionTarget by remember { mutableStateOf<RecurringTask?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (rules.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No recurring tasks.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rules, key = { it.id }) { rule ->
                    RecurringRuleRow(
                        rule = rule,
                        onToggle = { viewModel.toggleActive(rule) },
                        onLongClick = { actionTarget = rule },
                    )
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
            Icon(Icons.Default.Add, contentDescription = "Add recurring task")
        }
    }

    if (showAddSheet) {
        RecurringTaskSheet(
            existing = null,
            onDismiss = { showAddSheet = false },
            onSave = { title, freq, dow, dom, ord, mwd ->
                viewModel.add(title, freq, dow, dom, ord, mwd)
                showAddSheet = false
            },
        )
    }

    editingRule?.let { rule ->
        RecurringTaskSheet(
            existing = rule,
            onDismiss = { editingRule = null },
            onSave = { title, freq, dow, dom, ord, mwd ->
                viewModel.save(rule, title, freq, dow, dom, ord, mwd)
                editingRule = null
            },
        )
    }

    actionTarget?.let { rule ->
        RecurringActionSheet(
            rule = rule,
            onDismiss = { actionTarget = null },
            onEdit = { editingRule = rule; actionTarget = null },
            onDelete = { viewModel.delete(rule); actionTarget = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecurringRuleRow(
    rule: RecurringTask,
    onToggle: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (rule.isActive) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = rule.scheduleDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Switch(
                checked = rule.isActive,
                onCheckedChange = { onToggle() },
            )
        }
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

// ── Add / Edit sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecurringTaskSheet(
    existing: RecurringTask?,
    onDismiss: () -> Unit,
    onSave: (title: String, frequency: Frequency, dayOfWeek: Int?, dayOfMonth: Int?, monthlyOrdinal: Int?, monthlyWeekDay: Int?) -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var frequency by remember { mutableStateOf(existing?.frequency ?: Frequency.DAILY) }
    var selectedDow by remember { mutableIntStateOf(existing?.dayOfWeek ?: 5) } // Fri default
    var selectedDom by remember { mutableIntStateOf(existing?.dayOfMonth ?: 1) }
    var monthlyMode by remember {
        mutableStateOf(
            if (existing?.monthlyOrdinal != null) MonthlyMode.OnThe else MonthlyMode.OnDay
        )
    }
    var selectedOrdinal by remember { mutableIntStateOf(existing?.monthlyOrdinal ?: 1) }
    var selectedMonthlyDow by remember { mutableIntStateOf(existing?.monthlyWeekDay ?: 1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = if (existing == null) "New Recurring Task" else "Edit Recurring Task",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Task title") },
                maxLines = 2,
            )

            // Frequency selector
            Text(
                "Repeat",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { freq ->
                    FilterChip(
                        selected = frequency == freq,
                        onClick = { frequency = freq },
                        label = {
                            Text(freq.name.lowercase().replaceFirstChar { it.uppercase() })
                        },
                    )
                }
            }

            // Day-of-week picker (Weekly)
            if (frequency == Frequency.WEEKLY) {
                Text(
                    "Day",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { dow ->
                        val label = DayOfWeek.of(dow)
                            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        FilterChip(
                            selected = selectedDow == dow,
                            onClick = { selectedDow = dow },
                            label = { Text(label) },
                        )
                    }
                }
            }

            // Monthly picker with two sub-modes
            if (frequency == Frequency.MONTHLY) {
                Text(
                    "Schedule",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = monthlyMode == MonthlyMode.OnDay,
                        onClick = { monthlyMode = MonthlyMode.OnDay },
                        label = { Text("On day") },
                    )
                    FilterChip(
                        selected = monthlyMode == MonthlyMode.OnThe,
                        onClick = { monthlyMode = MonthlyMode.OnThe },
                        label = { Text("On the") },
                    )
                }

                if (monthlyMode == MonthlyMode.OnDay) {
                    // 5-row × 7-col inline grid for days 1–31
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0..4).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                (1..7).forEach { col ->
                                    val day = row * 7 + col
                                    if (day <= 31) {
                                        DayCell(
                                            day = day,
                                            isSelected = day == selectedDom,
                                            onClick = { selectedDom = day },
                                        )
                                    } else {
                                        Box(modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Ordinal picker
                    val ordinalOptions = listOf(
                        1 to "1st", 2 to "2nd", 3 to "3rd", 4 to "4th", -1 to "Last",
                    )
                    Text(
                        "Which",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ordinalOptions.forEach { (value, label) ->
                            FilterChip(
                                selected = selectedOrdinal == value,
                                onClick = { selectedOrdinal = value },
                                label = { Text(label) },
                            )
                        }
                    }

                    // Day-of-week picker
                    Text(
                        "Day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { dow ->
                            val label = DayOfWeek.of(dow)
                                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            FilterChip(
                                selected = selectedMonthlyDow == dow,
                                onClick = { selectedMonthlyDow = dow },
                                label = { Text(label) },
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        title,
                        frequency,
                        if (frequency == Frequency.WEEKLY) selectedDow else null,
                        if (frequency == Frequency.MONTHLY && monthlyMode == MonthlyMode.OnDay) selectedDom else null,
                        if (frequency == Frequency.MONTHLY && monthlyMode == MonthlyMode.OnThe) selectedOrdinal else null,
                        if (frequency == Frequency.MONTHLY && monthlyMode == MonthlyMode.OnThe) selectedMonthlyDow else null,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank(),
            ) {
                Text(if (existing == null) "Add" else "Save")
            }
        }
    }
}

// ── Long-press action sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurringActionSheet(
    rule: RecurringTask,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(rule.title, style = MaterialTheme.typography.titleMedium)
            Text(
                rule.scheduleDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Edit")
            }
            Button(
                onClick = { onDelete(); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isSelected: Boolean, onClick: () -> Unit) {
    val green = MaterialTheme.colorScheme.primary
    val onGreen = MaterialTheme.colorScheme.onPrimary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .then(
                if (isSelected) Modifier
                    .background(green, androidx.compose.foundation.shape.CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) onGreen else MaterialTheme.colorScheme.onSurface,
        )
    }
}
