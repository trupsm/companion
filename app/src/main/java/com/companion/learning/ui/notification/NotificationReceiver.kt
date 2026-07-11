package com.companion.learning.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.companion.learning.data.local.LearningDatabase
import com.companion.learning.data.local.security.SecureStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var database: LearningDatabase

    @Inject
    lateinit var secureStorage: SecureStorage

    private val quotes = listOf(
        "Consistency is the key to mastering any skill! 🔥",
        "Every small step brings you closer to your goal. 🚀",
        "Success is the sum of small efforts repeated day in and day out.",
        "Make today count! Your future self will thank you. 🎓",
        "Knowledge is power. Let's grow your mind today!",
        "Don't stop until you're proud. Keep going! 💪",
        "The secret of getting ahead is getting started.",
        "Study today, lead tomorrow. Let's build your future!"
    )

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Check if reminder is enabled
                if (!secureStorage.isStudyReminderEnabled()) {
                    pendingResult.finish()
                    return@launch
                }

                // 2. Check if user already studied or claimed a grace day today
                val today = LocalDate.now().toString()
                val logToday = database.streakDao.getLogForDate(today)
                if (logToday != null) {
                    // Already studied or used a grace day today, skip notification but reschedule
                    rescheduleAlarm(context)
                    pendingResult.finish()
                    return@launch
                }

                // 3. Find today's tasks shortly
                val activeRoadmaps = database.roadmapDao.getActiveRoadmaps()
                val todayDate = LocalDate.now()
                val taskTopics = mutableListOf<String>()

                activeRoadmaps.forEach { roadmap ->
                    val startedAt = roadmap.startedAt ?: return@forEach
                    val startLocalDate = Instant.ofEpochMilli(startedAt)
                        .atZone(ZoneId.systemDefault()).toLocalDate()

                    val daysDiff = ChronoUnit.DAYS.between(startLocalDate, todayDate)
                    val targetDayNumber = (daysDiff + 1).toInt()

                    if (targetDayNumber > 0) {
                        val items = database.curriculumDao.getCurriculumItemsForRoadmap(roadmap.id).first()
                        items.filter { it.dayNumber == targetDayNumber && it.status != "COMPLETED" }
                            .forEach { taskTopics.add(it.topic) }
                    }
                }

                // 4. Build notification message
                val quote = quotes.random()
                val taskSummary = if (taskTopics.isNotEmpty()) {
                    taskTopics.joinToString(", ") { it.take(30) + if (it.length > 30) "..." else "" }
                } else {
                    ""
                }

                // 5. Show notification
                NotificationHelper.showStudyReminderNotification(context, quote, taskSummary)

                // 6. Reschedule for tomorrow
                rescheduleAlarm(context)
            } catch (e: Exception) {
                Log.e("NotificationReceiver", "Error sending study reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun rescheduleAlarm(context: Context) {
        val hour = secureStorage.getStudyReminderHour()
        val minute = secureStorage.getStudyReminderMinute()
        scheduleDailyAlarm(context, hour, minute, secureStorage)
    }

    companion object {
        fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int, secureStorage: SecureStorage) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel any existing alarm first
            alarmManager.cancel(pendingIntent)

            if (!secureStorage.isStudyReminderEnabled()) return

            val now = LocalDateTime.now()
            var triggerTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, minute))
            
            // If the configured time is in the past, schedule for tomorrow
            if (triggerTime.isBefore(now) || triggerTime.isEqual(now)) {
                triggerTime = triggerTime.plusDays(1)
            }

            val triggerTimeMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationReceiver", "Scheduled study reminder alarm for $triggerTime")
        }
    }
}
