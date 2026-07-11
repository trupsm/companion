package com.companion.learning.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val apiKey by viewModel.apiKey.collectAsState()
    val studyHours by viewModel.studyHours.collectAsState()
    val username by viewModel.username.collectAsState()
    
    var selectedProvider by remember { mutableStateOf("OpenAI") }
    var graceDays by remember { mutableStateOf("1") }
    var resourcesPref by remember { mutableStateOf("Free & Paid") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Settings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Profile Settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("User Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider()

            // LLM Settings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI Provider Settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Text("Selected Provider", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("OpenAI", "Gemini", "Claude").forEach { provider ->
                        FilterChip(
                            selected = selectedProvider == provider,
                            onClick = { selectedProvider = provider },
                            label = { Text(provider) }
                        )
                    }
                }

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = { Text("API Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = { viewModel.clearApiKey() }) {
                    Text("Clear API Key")
                }
            }

            HorizontalDivider()

            // Learning Settings
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Learning Preferences", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = graceDays,
                    onValueChange = { graceDays = it },
                    label = { Text("Grace days per month (Streaks)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = studyHours,
                    onValueChange = { viewModel.updateStudyHours(it) },
                    label = { Text("Study hours dedicated per day") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Resource Recommendations", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Free Only", "Paid Only", "Free & Paid").forEach { pref ->
                        FilterChip(
                            selected = resourcesPref == pref,
                            onClick = { resourcesPref = pref },
                            label = { Text(pref) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Reminder Settings
            val reminderEnabled by viewModel.studyReminderEnabled.collectAsState()
            val reminderHour by viewModel.studyReminderHour.collectAsState()
            val reminderMinute by viewModel.studyReminderMinute.collectAsState()

            val formattedTime = remember(reminderHour, reminderMinute) {
                val amPm = if (reminderHour >= 12) "PM" else "AM"
                val hourIn12 = when {
                    reminderHour == 0 -> 12
                    reminderHour > 12 -> reminderHour - 12
                    else -> reminderHour
                }
                String.format("%02d:%02d %s", hourIn12, reminderMinute, amPm)
            }

            val timePickerDialog = remember(reminderHour, reminderMinute) {
                android.app.TimePickerDialog(
                    context,
                    { _, selectedHour: Int, selectedMinute: Int ->
                        viewModel.updateStudyReminderHour(selectedHour)
                        viewModel.updateStudyReminderMinute(selectedMinute)
                    },
                    reminderHour,
                    reminderMinute,
                    false
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Study Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Reminder", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Get notified to maintain your streak",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { viewModel.updateStudyReminderEnabled(it) }
                            )
                        }

                        if (reminderEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reminder Time", style = MaterialTheme.typography.bodyLarge)
                                Button(
                                    onClick = { timePickerDialog.show() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Text(formattedTime)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Button(
                onClick = { 
                    viewModel.saveSettings()
                    Toast.makeText(context, "API Key and Settings Saved!", Toast.LENGTH_SHORT).show()
                    onNavigateBack() 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}
