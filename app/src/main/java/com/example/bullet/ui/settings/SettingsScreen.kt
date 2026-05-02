package com.example.bullet.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val pushedTasksEnabled by viewModel.pushedTasksNotification.collectAsStateWithLifecycle()
    val dailyReminderEnabled by viewModel.dailyReminder.collectAsStateWithLifecycle()

    val permissionLauncher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { /* result handled by system */ }
    } else {
        null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        ListItem(
            headlineContent = { Text("Pushed task alerts") },
            supportingContent = { Text("Notify when tasks are carried over from yesterday") },
            trailingContent = {
                Switch(
                    checked = pushedTasksEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setPushedTasksNotification(enabled)
                    },
                )
            },
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        ListItem(
            headlineContent = { Text("Daily reminder") },
            supportingContent = { Text("Get a daily summary of today's open tasks") },
            trailingContent = {
                Switch(
                    checked = dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setDailyReminder(enabled)
                    },
                )
            },
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
