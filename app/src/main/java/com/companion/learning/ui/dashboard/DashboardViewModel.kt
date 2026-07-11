package com.companion.learning.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.data.local.LearningDatabase
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.domain.repository.RoadmapRepository
import com.companion.learning.domain.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class AnalyticsData(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0   // past-day tasks still NOT_STARTED / IN_PROGRESS
)

data class DashboardUiState(
    val username: String = "Learner",
    val selectedDate: LocalDate = LocalDate.now(),
    val activeRoadmaps: List<RoadmapEntity> = emptyList(),
    val tasksForSelectedDate: List<CurriculumItemEntity> = emptyList(),
    val datesWithPendingTasks: Set<LocalDate> = emptySet(),
    val analytics: AnalyticsData = AnalyticsData(),
    val todayProgress: Float = 0f,   // 0..1 fraction of today's tasks completed
    val streakCount: Int = 0,
    val availableGraceDays: Int = 0,
    val hasStudiedToday: Boolean = false,
    val isGraceDayLoggedToday: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository,
    private val streakRepository: StreakRepository,
    private val database: LearningDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(username = secureStorage.getUsername()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        updateTasksForDate(date, _uiState.value.activeRoadmaps)
    }

    fun refreshUsername() {
        _uiState.update { it.copy(username = secureStorage.getUsername()) }
        loadStreakAndGrace()
    }

    fun loadData() {
        viewModelScope.launch {
            roadmapRepository.getAllRoadmaps().collectLatest { roadmaps ->
                val active = roadmaps.filter { it.status == "ACTIVE" }
                _uiState.update {
                    it.copy(activeRoadmaps = active, username = secureStorage.getUsername())
                }
                computeAnalyticsAndPendingDates(active)
                updateTasksForDate(_uiState.value.selectedDate, active)
                loadStreakAndGrace()
            }
        }
    }

    private fun loadStreakAndGrace() {
        viewModelScope.launch {
            val streak = streakRepository.getActiveStreak()
            val graceDays = streakRepository.getAvailableGraceDays()
            val todayStr = LocalDate.now().toString()
            val logToday = database.streakDao.getLogForDate(todayStr)
            
            val hasStudied = logToday != null && !logToday.isGraceDay
            val isGraceDay = logToday != null && logToday.isGraceDay
            
            _uiState.update {
                it.copy(
                    streakCount = streak,
                    availableGraceDays = graceDays,
                    hasStudiedToday = hasStudied,
                    isGraceDayLoggedToday = isGraceDay
                )
            }
        }
    }

    fun claimGraceDay() {
        viewModelScope.launch {
            val success = streakRepository.claimGraceDayToday()
            if (success) {
                loadStreakAndGrace()
            }
        }
    }

    private fun computeAnalyticsAndPendingDates(activeRoadmaps: List<RoadmapEntity>) {
        viewModelScope.launch {
            val today = LocalDate.now()
            var total = 0
            var completed = 0
            var overdue = 0
            val pendingDates = mutableSetOf<LocalDate>()

            activeRoadmaps.forEach { roadmap ->
                val startedAt = roadmap.startedAt ?: return@forEach
                val startDate = Instant.ofEpochMilli(startedAt)
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                val allItems = curriculumRepository.getCurriculumItemsForRoadmap(roadmap.id).first()
                total += allItems.size
                val comp = allItems.count { it.status == "COMPLETED" }
                completed += comp

                // Find items that belonged to past days and are not completed
                allItems.forEach { item ->
                    val itemDate = startDate.plusDays((item.dayNumber - 1).toLong())
                    if (itemDate.isBefore(today) && item.status != "COMPLETED") {
                        overdue++
                        pendingDates.add(itemDate)
                    }
                    if (itemDate == today && item.status != "COMPLETED") {
                        pendingDates.add(itemDate)
                    }
                }
            }

            // Today's progress
            val todayTasks = _uiState.value.tasksForSelectedDate.let {
                if (_uiState.value.selectedDate == today) it
                else {
                    // Recompute today's tasks for progress bar
                    mutableListOf<CurriculumItemEntity>().also { list ->
                        activeRoadmaps.forEach { roadmap ->
                            val startedAt = roadmap.startedAt ?: return@forEach
                            val startDate = Instant.ofEpochMilli(startedAt)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val daysDiff = ChronoUnit.DAYS.between(startDate, today)
                            val dayNum = (daysDiff + 1).toInt()
                            if (dayNum > 0) {
                                curriculumRepository.getCurriculumItemsForRoadmap(roadmap.id)
                                    .first()
                                    .filter { it.dayNumber == dayNum }
                                    .forEach { list.add(it) }
                            }
                        }
                    }
                }
            }
            val todayProgress = if (todayTasks.isEmpty()) 0f
            else todayTasks.count { it.status == "COMPLETED" }.toFloat() / todayTasks.size

            _uiState.update {
                it.copy(
                    analytics = AnalyticsData(
                        totalTasks = total,
                        completedTasks = completed,
                        pendingTasks = total - completed - overdue,
                        overdueTasks = overdue
                    ),
                    datesWithPendingTasks = pendingDates,
                    todayProgress = todayProgress,
                    isLoading = false
                )
            }
        }
    }

    private fun updateTasksForDate(date: LocalDate, activeRoadmaps: List<RoadmapEntity>) {
        if (activeRoadmaps.isEmpty()) {
            _uiState.update { it.copy(tasksForSelectedDate = emptyList(), isLoading = false) }
            return
        }

        viewModelScope.launch {
            val allTasks = mutableListOf<CurriculumItemEntity>()

            activeRoadmaps.forEach { roadmap ->
                val startedAt = roadmap.startedAt ?: return@forEach
                val startLocalDate = Instant.ofEpochMilli(startedAt)
                    .atZone(ZoneId.systemDefault()).toLocalDate()

                val daysDiff = ChronoUnit.DAYS.between(startLocalDate, date)
                val targetDayNumber = (daysDiff + 1).toInt()

                if (targetDayNumber > 0) {
                    curriculumRepository.getCurriculumItemsForRoadmap(roadmap.id)
                        .first()
                        .filter { it.dayNumber == targetDayNumber }
                        .forEach { allTasks.add(it) }
                }
            }

            _uiState.update {
                it.copy(tasksForSelectedDate = allTasks, isLoading = false)
            }
        }
    }
}
