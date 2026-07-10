package com.companion.learning.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoadmapScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateRoadmapViewModel = hiltViewModel()
) {
    var goal by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("3 months") }
    var level by remember { mutableStateOf("Beginner") }
    var hoursPerDay by remember { mutableStateOf("2") }
    
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is GenerationState.Success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Roadmap") },
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
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text("Learning Goal (e.g., Become a SOC Analyst)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is GenerationState.Loading
            )

            OutlinedTextField(
                value = hoursPerDay,
                onValueChange = { hoursPerDay = it },
                label = { Text("Daily Study Hours for this Course") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is GenerationState.Loading
            )

            Text("Duration", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1 month", "3 months", "6 months").forEach { option ->
                    FilterChip(
                        selected = duration == option,
                        onClick = { duration = option },
                        label = { Text(option) },
                        enabled = uiState !is GenerationState.Loading
                    )
                }
            }

            Text("Experience Level", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Beginner", "Intermediate", "Advanced").forEach { option ->
                    FilterChip(
                        selected = level == option,
                        onClick = { level = option },
                        label = { Text(option) },
                        enabled = uiState !is GenerationState.Loading
                    )
                }
            }

            if (uiState is GenerationState.Error) {
                Text(
                    text = (uiState as GenerationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    val hours = hoursPerDay.toIntOrNull() ?: 2
                    viewModel.generateRoadmap(goal, duration, level, hours) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = goal.isNotBlank() && hoursPerDay.isNotBlank() && uiState !is GenerationState.Loading
            ) {
                if (uiState is GenerationState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Generate AI Roadmap")
                }
            }
        }
    }
}
