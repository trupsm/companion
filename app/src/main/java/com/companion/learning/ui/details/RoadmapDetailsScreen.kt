package com.companion.learning.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.MilestoneEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapDetailsScreen(
    onNavigateBack: () -> Unit,
    onTopicClick: (topicId: String, roadmapGoal: String) -> Unit,
    viewModel: RoadmapDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editTitle by remember { mutableStateOf("") }
    var editGoal by remember { mutableStateOf("") }

    // Initialize edit fields when roadmap loads
    LaunchedEffect(uiState.roadmap) {
        uiState.roadmap?.let {
            editTitle = it.title
            editGoal = it.goal
        }
    }

    // Trigger lazy expansion when tab selection changes
    LaunchedEffect(selectedTab, uiState.milestones) {
        viewModel.checkAndExpandMilestone(selectedTab)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Roadmap") },
            text = { Text("Are you sure you want to delete this roadmap? All your progress, milestones, and daily tasks will be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRoadmap { onNavigateBack() }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Roadmap Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Roadmap Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editGoal,
                        onValueChange = { editGoal = it },
                        label = { Text("Learning Goal") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        viewModel.updateRoadmap(editTitle, editGoal)
                    },
                    enabled = editTitle.isNotBlank() && editGoal.isNotBlank()
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.roadmap?.title ?: "Roadmap Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.roadmap != null) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (uiState.roadmap?.status == "ACTIVE") {
                                DropdownMenuItem(
                                    text = { Text("Pause Learning") },
                                    leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.pauseRoadmap()
                                    }
                                )
                            } else if (uiState.roadmap?.status == "PAUSED") {
                                DropdownMenuItem(
                                    text = { Text("Resume Learning") },
                                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                    onClick = {
                                        showMenu = false
                                        viewModel.resumeRoadmap()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Edit Details") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Roadmap") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val roadmap = uiState.roadmap
            if (roadmap == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Roadmap not found.")
                }
            } else {
                val milestones = uiState.milestones
                val curriculumItems = uiState.curriculumItems

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Start Roadmap Overlay if NOT_YET_STARTED
                    if (roadmap.status == "NOT_YET_STARTED") {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Roadmap Not Started", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Start this roadmap to schedule daily tasks on your Dashboard calendar.", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = { viewModel.startRoadmap() }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start")
                                }
                            }
                        }
                    }

                    // Resume Roadmap Overlay if PAUSED
                    if (roadmap.status == "PAUSED") {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Roadmap Paused", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text("Resume this roadmap to continue scheduling daily tasks on your Dashboard calendar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f))
                                }
                                Button(
                                    onClick = { viewModel.resumeRoadmap() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume")
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Goal: ${roadmap.goal}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Duration: ${roadmap.duration} | Status: ${roadmap.status.replace("_", " ")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (milestones.isNotEmpty()) {
                        val tabIndex = selectedTab.coerceIn(0, milestones.size - 1)
                        if (selectedTab != tabIndex) {
                            selectedTab = tabIndex
                        }

                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            milestones.forEachIndexed { index, milestone ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text("Week ${milestone.weekNumber}") }
                                )
                            }
                        }

                        val activeMilestone = milestones[selectedTab]
                        val filteredTasks = curriculumItems.filter { it.milestoneId == activeMilestone.id }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                MilestoneSummaryCard(milestone = activeMilestone)
                            }

                            item {
                                Text(
                                    text = "Weekly Topics",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                             if (filteredTasks.isEmpty()) {
                                 if (uiState.isExpanding || activeMilestone.expansionStatus == "EXPANDING") {
                                     item {
                                         Box(
                                             modifier = Modifier.fillMaxWidth().height(180.dp),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             Column(
                                                 horizontalAlignment = Alignment.CenterHorizontally,
                                                 verticalArrangement = Arrangement.spacedBy(8.dp)
                                             ) {
                                                 CircularProgressIndicator()
                                                 Text(
                                                     "Generating Week ${activeMilestone.weekNumber} study topics...",
                                                     style = MaterialTheme.typography.bodyMedium,
                                                     color = MaterialTheme.colorScheme.onSurfaceVariant
                                                 )
                                             }
                                         }
                                     }
                                 } else if (uiState.expansionError != null) {
                                     item {
                                         val errorMsg = uiState.expansionError.orEmpty()
                                         Card(
                                             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                             modifier = Modifier.fillMaxWidth()
                                         ) {
                                             Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                 Text(
                                                     text = errorMsg,
                                                     color = MaterialTheme.colorScheme.onErrorContainer,
                                                     style = MaterialTheme.typography.bodyMedium
                                                 )
                                                 Button(
                                                     onClick = { viewModel.checkAndExpandMilestone(selectedTab) },
                                                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                 ) {
                                                     Text("Retry Generation")
                                                 }
                                             }
                                         }
                                     }
                                 } else {
                                     item {
                                         Text(
                                             text = "No topics generated for this week yet.",
                                             style = MaterialTheme.typography.bodyMedium,
                                             color = MaterialTheme.colorScheme.onSurfaceVariant
                                         )
                                     }
                                 }
                             } else {
                                 items(filteredTasks) { task ->
                                     TopicItemCard(
                                         task = task,
                                         onClick = { onTopicClick(task.id, roadmap.goal) }
                                     )
                                 }
                             }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No milestones found.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneSummaryCard(
    milestone: MilestoneEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = milestone.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${milestone.expansionStatus}",
                style = MaterialTheme.typography.labelSmall,
                color = if (milestone.expansionStatus == "EXPANDED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun TopicItemCard(
    task: CurriculumItemEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == "COMPLETED"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isCompleted) "Completed" else "Pending",
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Day ${task.dayNumber}: ${task.topic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                task.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
