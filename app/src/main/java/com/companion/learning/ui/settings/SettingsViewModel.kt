package com.companion.learning.ui.settings

import androidx.lifecycle.ViewModel
import com.companion.learning.data.local.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _apiKey = MutableStateFlow(secureStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _studyHours = MutableStateFlow(secureStorage.getStudyHours())
    val studyHours: StateFlow<String> = _studyHours.asStateFlow()

    private val _username = MutableStateFlow(secureStorage.getUsername())
    val username: StateFlow<String> = _username.asStateFlow()

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun updateStudyHours(hours: String) {
        _studyHours.value = hours
    }

    fun updateUsername(name: String) {
        _username.value = name
    }

    fun saveSettings() {
        secureStorage.saveApiKey(_apiKey.value)
        secureStorage.saveStudyHours(_studyHours.value)
        secureStorage.saveUsername(_username.value)
    }

    fun clearApiKey() {
        secureStorage.clearApiKey()
        _apiKey.value = ""
    }
}
