package com.companion.learning.ui.importroadmap

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportRoadmapScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportRoadmapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var roadmapTitle by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("2") }
    var duration by remember { mutableStateOf("3 months") }
    var level by remember { mutableStateOf("Beginner") }
    var filePickerLabel by remember { mutableStateOf("No file selected") }
    var importMethod by remember { mutableStateOf(0) } // 0 = File, 1 = Paste Text
    var pastedText by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = uri.lastPathSegment ?: "document"
            filePickerLabel = fileName
            if (roadmapTitle.isBlank()) {
                roadmapTitle = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
            }
            viewModel.parseDocument(
                uri = uri, 
                title = roadmapTitle.ifBlank { "My Roadmap" },
                hours = hours,
                duration = duration,
                level = level
            )
        }
    }

    LaunchedEffect(state) {
        if (state is ImportState.Success) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Roadmap") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val currentState = state) {

                is ImportState.Idle, is ImportState.Error -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = roadmapTitle,
                            onValueChange = { roadmapTitle = it },
                            label = { Text("Roadmap Title / Goal") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., Learn Linux Security") }
                        )

                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it },
                            label = { Text("Study Hours Dedicated per Day") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g., 2") }
                        )

                        Text("Duration", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1 month", "3 months", "6 months").forEach { option ->
                                FilterChip(
                                    selected = duration == option,
                                    onClick = { duration = option },
                                    label = { Text(option) }
                                )
                            }
                        }

                        Text("Experience Level", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { option ->
                                FilterChip(
                                    selected = level == option,
                                    onClick = { level = option },
                                    label = { Text(option) }
                                )
                            }
                        }



                        TabRow(
                            selectedTabIndex = importMethod,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        ) {
                            Tab(
                                selected = importMethod == 0,
                                onClick = { importMethod = 0 },
                                text = { Text("Upload File") }
                            )
                            Tab(
                                selected = importMethod == 1,
                                onClick = { importMethod = 1 },
                                text = { Text("Paste Text") }
                            )
                        }

                        if (importMethod == 0) {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    filePicker.launch("*/*")
                                },
                                enabled = roadmapTitle.isNotBlank()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = if (roadmapTitle.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                    Text("Tap to Select File", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Supported formats: PDF, DOCX, TXT",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (filePickerLabel != "No file selected") {
                                        Text(
                                            "Selected: $filePickerLabel",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = pastedText,
                                    onValueChange = { pastedText = it },
                                    label = { Text("Paste raw roadmap/syllabus text here...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    maxLines = 10
                                )

                                Button(
                                    onClick = {
                                        viewModel.parseRawText(
                                            rawText = pastedText,
                                            title = roadmapTitle.ifBlank { "My Roadmap" },
                                            hours = hours,
                                            duration = duration,
                                            level = level
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = roadmapTitle.isNotBlank() && pastedText.isNotBlank()
                                ) {
                                    Text("Convert & Generate Roadmap")
                                }
                            }
                        }

                        if (currentState is ImportState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is ImportState.Parsing -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator()
                            Text("Reading your document...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                is ImportState.Preview -> {
                    Text("Review Your Roadmap", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "We found ${currentState.dto.milestones.size} milestones in your document. Review and confirm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.dto.milestones) { milestone ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Week ${milestone.weekNumber}: ${milestone.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = milestone.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.reset() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Choose Different File") }

                        Button(
                            onClick = { 
                                viewModel.saveRoadmap(
                                    dto = currentState.dto, 
                                    goal = currentState.title,
                                    hours = currentState.hours,
                                    duration = currentState.duration,
                                    level = currentState.level
                                ) 
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Save Roadmap")
                        }
                    }
                }

                is ImportState.Saving -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator()
                            Text("Saving your roadmap...", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
