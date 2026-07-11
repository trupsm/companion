package com.companion.learning.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.ui.notification.NotificationReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _studyHours = MutableStateFlow(secureStorage.getStudyHours())
    val studyHours: StateFlow<String> = _studyHours.asStateFlow()

    private val _username = MutableStateFlow(secureStorage.getUsername())
    val username: StateFlow<String> = _username.asStateFlow()

    private val _studyReminderEnabled = MutableStateFlow(secureStorage.isStudyReminderEnabled())
    val studyReminderEnabled: StateFlow<Boolean> = _studyReminderEnabled.asStateFlow()

    private val _studyReminderHour = MutableStateFlow(secureStorage.getStudyReminderHour())
    val studyReminderHour: StateFlow<Int> = _studyReminderHour.asStateFlow()

    private val _studyReminderMinute = MutableStateFlow(secureStorage.getStudyReminderMinute())
    val studyReminderMinute: StateFlow<Int> = _studyReminderMinute.asStateFlow()

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun updateStudyHours(hours: String) {
        _studyHours.value = hours
    }

    fun updateUsername(name: String) {
        _username.value = name
    }

    fun updateStudyReminderEnabled(enabled: Boolean) {
        _studyReminderEnabled.value = enabled
    }

    fun updateStudyReminderHour(hour: Int) {
        _studyReminderHour.value = hour
    }

    fun updateStudyReminderMinute(minute: Int) {
        _studyReminderMinute.value = minute
    }

    fun saveSettings() {
        secureStorage.saveApiKey(_apiKey.value)
        secureStorage.saveStudyHours(_studyHours.value)
        secureStorage.saveUsername(_username.value)
        secureStorage.setStudyReminderEnabled(_studyReminderEnabled.value)
        secureStorage.setStudyReminderHour(_studyReminderHour.value)
        secureStorage.setStudyReminderMinute(_studyReminderMinute.value)

        // Reschedule the alarm according to new settings
        NotificationReceiver.scheduleDailyAlarm(
            context,
            _studyReminderHour.value,
            _studyReminderMinute.value,
            secureStorage
        )
    }

    fun clearApiKey() {
        secureStorage.clearApiKey()
        _apiKey.value = ""
    }
}
