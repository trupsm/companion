package com.companion.learning.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.companion.learning.data.local.entity.CurriculumItemEntity
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTaskClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshUsername()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Welcome back,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(uiState.username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        if (uiState.streakCount > 0) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                                Text("${uiState.streakCount} days", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Today's Progress ──────────────────────────────────
            item {
                TodayProgressCard(
                    progress = uiState.todayProgress,
                    completed = uiState.analytics.completedTasks,
                    total = uiState.analytics.totalTasks
                )
            }

            // ── Streak & Grace Days Card ──────────────────────────
            item {
                StreakGraceCard(
                    streakCount = uiState.streakCount,
                    graceDays = uiState.availableGraceDays,
                    hasStudiedToday = uiState.hasStudiedToday,
                    isGraceDayLogged = uiState.isGraceDayLoggedToday,
                    onClaimGraceDay = { viewModel.claimGraceDay() }
                )
            }

            // ── Analytics Overview ────────────────────────────────
            item {
                AnalyticsCard(analytics = uiState.analytics)
            }

            // ── Calendar ──────────────────────────────────────────
            item {
                Text("Calendar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                val today = LocalDate.now()
                val weekDays = (-3..3).map { today.plusDays(it.toLong()) }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(weekDays) { date ->
                        val isSelected = date == uiState.selectedDate
                        val hasPending = uiState.datesWithPendingTasks.contains(date)
                        CalendarDayCard(
                            date = date,
                            isSelected = isSelected,
                            hasPendingIndicator = hasPending,
                            onClick = { viewModel.selectDate(date) }
                        )
                    }
                }
            }

            // ── Tasks for selected date ───────────────────────────
            item {
                val today = LocalDate.now()
                val selectedDateText = if (uiState.selectedDate == today)
                    "Today's Tasks"
                else
                    "Tasks for ${uiState.selectedDate.dayOfMonth} ${uiState.selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                Text(selectedDateText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                // Day progress bar
                if (uiState.tasksForSelectedDate.isNotEmpty()) {
                    val dayCompleted = uiState.tasksForSelectedDate.count { it.status == "COMPLETED" }
                    val dayTotal = uiState.tasksForSelectedDate.size
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$dayCompleted / $dayTotal tasks completed",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (dayTotal > 0) dayCompleted.toFloat() / dayTotal else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (dayCompleted == dayTotal && dayTotal > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (uiState.tasksForSelectedDate.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("No tasks scheduled for this day.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Relax or write down some notes!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            } else {
                items(uiState.tasksForSelectedDate) { task ->
                    TaskItemCard(
                        task = task,
                        onClick = { onTaskClick(task.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun TodayProgressCard(progress: Float, completed: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Overall Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$completed / $total topics complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun StreakGraceCard(
    streakCount: Int,
    graceDays: Int,
    hasStudiedToday: Boolean,
    isGraceDayLogged: Boolean,
    onClaimGraceDay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daily Learning Streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Study daily to keep your streak alive!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streakCount > 0) Color(0xFFFF5722) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(36.dp)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                    Column {
                        Text("Available Grace Days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("$graceDays / 3 active protection days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                // Status / Claim button
                when {
                    hasStudiedToday -> {
                        Text(
                            "Today Logged! 🔥",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    isGraceDayLogged -> {
                        Text(
                            "Grace Day Applied 🛡️",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    graceDays > 0 -> {
                        Button(
                            onClick = onClaimGraceDay,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Use Grace Day")
                        }
                    }
                    else -> {
                        Text(
                            "0 Grace Days left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (!hasStudiedToday && !isGraceDayLogged) {
                Text(
                    text = "*Earn 1 Grace Day (max 3) for every 7 days streak milestone you hit!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun AnalyticsCard(analytics: AnalyticsData) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Analytics Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AnalyticsStat(label = "Total", value = analytics.totalTasks.toString(), color = MaterialTheme.colorScheme.onSurface)
                AnalyticsStat(label = "Done", value = analytics.completedTasks.toString(), color = MaterialTheme.colorScheme.primary)
                AnalyticsStat(label = "Pending", value = analytics.pendingTasks.toString(), color = MaterialTheme.colorScheme.secondary)
                AnalyticsStat(label = "Overdue", value = analytics.overdueTasks.toString(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AnalyticsStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CalendarDayCard(
    date: LocalDate,
    isSelected: Boolean,
    hasPendingIndicator: Boolean = false,
    onClick: () -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayNumber = date.dayOfMonth.toString()
    val isToday = date == LocalDate.now()

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(dayName, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
            Text(dayNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            
            // Dot indicator for pending tasks
            if (hasPendingIndicator) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                )
            } else {
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: CurriculumItemEntity,
    onClick: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.topic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Day ${task.dayNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
