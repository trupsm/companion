package com.companion.learning.ui.topic

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.companion.learning.data.remote.dto.ResourceDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTopicScreen(
    onNavigateBack: () -> Unit,
    onOpenNotes: () -> Unit,
    onTakeQuiz: () -> Unit,
    onMarkComplete: () -> Unit,
    roadmapGoal: String = "",
    viewModel: TodayTopicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load resources once item is available
    LaunchedEffect(uiState.item) {
        if (uiState.item != null && roadmapGoal.isNotBlank()) {
            viewModel.loadResources(roadmapGoal)
        }
    }

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
                        Text(
                            "This topic may not have content generated yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

                    // Status card with color-coded badge
                    SurfaceCard(title = "Status") {
                        val (label, color) = when (topic.status) {
                            "COMPLETED" -> "✅ Completed" to MaterialTheme.colorScheme.primary
                            "IN_PROGRESS" -> "🔄 In Progress" to MaterialTheme.colorScheme.secondary
                            "SKIPPED" -> "⏭ Skipped" to MaterialTheme.colorScheme.outline
                            else -> "⬜ Not Started" to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(text = label, color = color, fontWeight = FontWeight.Bold)
                    }

                    SurfaceCard(title = "Description") {
                        Text(
                            text = topic.description ?: "No description provided for this topic.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Resources card
                    SurfaceCard(title = "Resources") {
                        when {
                            uiState.isLoadingResources -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text(
                                        "Loading resources…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            uiState.resources.isNotEmpty() -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.resources.forEach { resource ->
                                        ResourceChip(resource = resource) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                            }
                            uiState.resourceError != null -> {
                                Text(
                                    text = "Could not load resources. ${uiState.resourceError}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            roadmapGoal.isBlank() -> {
                                Text(
                                    "Open from a roadmap to load AI-recommended resources.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onOpenNotes, modifier = Modifier.weight(1f)) {
                            Text("Notes")
                        }
                        Button(
                            onClick = onTakeQuiz,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.hasQuiz)
                                    MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (uiState.hasQuiz) "Retake Quiz" else "Quiz")
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
private fun ResourceChip(resource: ResourceDto, onClick: () -> Unit) {
    val icon: ImageVector = when (resource.type.uppercase()) {
        "VIDEO" -> Icons.Default.PlayCircle
        "DOCS" -> Icons.Default.Article
        "PRACTICE" -> Icons.Default.Code
        else -> Icons.Default.Link
    }
    val chipColor = when (resource.type.uppercase()) {
        "VIDEO" -> MaterialTheme.colorScheme.tertiaryContainer
        "DOCS" -> MaterialTheme.colorScheme.secondaryContainer
        "PRACTICE" -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = chipColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = resource.type, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(resource.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    resource.url.take(45) + if (resource.url.length > 45) "…" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                resource.type.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
