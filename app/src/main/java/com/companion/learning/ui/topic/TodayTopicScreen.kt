package com.companion.learning.ui.topic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTopicScreen(
    onNavigateBack: () -> Unit,
    onOpenNotes: () -> Unit,
    onTakeQuiz: () -> Unit,
    onMarkComplete: () -> Unit,
    viewModel: TodayTopicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.item != null) "Day ${uiState.item!!.dayNumber}" else "Topic") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.notFound -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Topic not found.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("This topic may not have content generated yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            else -> {
                val topic = uiState.item!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = topic.topic,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    topic.estimatedTime?.let {
                        Text(
                            text = "Estimated Time: $it mins",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    SurfaceCard(title = "Description") {
                        Text(
                            text = topic.description ?: "No description provided for this topic.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    SurfaceCard(title = "Status") {
                        val color = when (topic.status) {
                            "COMPLETED" -> MaterialTheme.colorScheme.primary
                            "IN_PROGRESS" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = topic.status.replace("_", " "),
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    SurfaceCard(title = "Resources") {
                        Text(
                            text = "• Official Documentation\n• YouTube Tutorials\n• Practice Exercises",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f, fill = false))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenNotes, modifier = Modifier.weight(1f)) {
                            Text("Notes")
                        }
                        OutlinedButton(onClick = onTakeQuiz, modifier = Modifier.weight(1f)) {
                            Text("Quiz")
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.markTopicCompleted()
                            onMarkComplete()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = topic.status != "COMPLETED"
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (topic.status == "COMPLETED") "Already Completed ✓" else "Mark as Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun SurfaceCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
