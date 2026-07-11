package com.companion.learning.data.local.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(key: String) {
        sharedPreferences.edit().putString("API_KEY", key).apply()
    }

    fun getApiKey(): String? {
        return sharedPreferences.getString("API_KEY", null)
    }

    fun clearApiKey() {
        sharedPreferences.edit().remove("API_KEY").apply()
    }

    fun saveStudyHours(hours: String) {
        sharedPreferences.edit().putString("STUDY_HOURS", hours).apply()
    }

    fun getStudyHours(): String {
        return sharedPreferences.getString("STUDY_HOURS", "2") ?: "2"
    }

    fun saveUsername(name: String) {
        sharedPreferences.edit().putString("USERNAME", name).apply()
    }

    fun getUsername(): String {
        return sharedPreferences.getString("USERNAME", "Learner") ?: "Learner"
    }

    fun getAvailableGraceDays(): Int {
        return sharedPreferences.getInt("AVAILABLE_GRACE_DAYS", -1)
    }

    fun setAvailableGraceDays(count: Int) {
        sharedPreferences.edit().putInt("AVAILABLE_GRACE_DAYS", count).apply()
    }

    fun isStudyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean("STUDY_REMINDER_ENABLED", true)
    }

    fun setStudyReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("STUDY_REMINDER_ENABLED", enabled).apply()
    }

    fun getStudyReminderHour(): Int {
        return sharedPreferences.getInt("STUDY_REMINDER_HOUR", 9)
    }

    fun setStudyReminderHour(hour: Int) {
        sharedPreferences.edit().putInt("STUDY_REMINDER_HOUR", hour).apply()
    }

    fun getStudyReminderMinute(): Int {
        return sharedPreferences.getInt("STUDY_REMINDER_MINUTE", 0)
    }

    fun setStudyReminderMinute(minute: Int) {
        sharedPreferences.edit().putInt("STUDY_REMINDER_MINUTE", minute).apply()
    }
}
