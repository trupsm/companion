package com.companion.learning.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.companion.learning.MainActivity

object NotificationHelper {
    private const val REMINDER_CHANNEL_ID = "daily_study_reminders"
    private const val REMINDER_CHANNEL_NAME = "Daily Study Reminders"
    private const val REMINDER_CHANNEL_DESC = "Reminders to complete scheduled tasks and maintain your learning streak."
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = REMINDER_CHANNEL_DESC
            }
            
            manager.createNotificationChannel(channel)
        }
    }

    fun showStudyReminderNotification(context: Context, quote: String, taskSummary: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app when tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val messageBody = if (taskSummary.isNotBlank()) {
            "$quote\n\nToday's Focus: $taskSummary"
        } else {
            "$quote\n\nNo scheduled tasks today, but you can always review or write notes!"
        }

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System default icon for compatibility
            .setContentTitle("Learning Companion: Time to Study! 📚")
            .setContentText(quote)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
