package com.example.bullet.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bullet.data.db.Task
import com.example.bullet.ui.calendar.CalendarViewModel
import com.example.bullet.ui.shared.AddEntrySheet
import com.example.bullet.ui.shared.TaskActionSheet
import com.example.bullet.ui.shared.TaskRow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    LaunchedEffect(Unit) { viewModel.goToToday() }

    var showAddSheet by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Text(
                    text = "Today · ${today.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 24.dp),
                )
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                if (tasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Nothing scheduled for today",
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
            defaultDate = today,
            onDismiss = { showAddSheet = false },
            onAdd = { content, date ->
                viewModel.addTask(content, date)
                showAddSheet = false
            },
        )
    }

    selectedTask?.let { task ->
        TaskActionSheet(
            task = task,
            onDismiss = { selectedTask = null },
            onSave = { content, date -> viewModel.saveTaskEdits(task, content, date) },
            onDelete = { viewModel.deleteTask(task) },
        )
    }
}
