package com.companion.learning.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.dao.UserDao
import com.companion.learning.data.local.entity.UserEntity
import com.companion.learning.data.local.security.PasswordHasher
import com.companion.learning.data.local.security.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun isUserLoggedIn(): Boolean {
        // If a username exists in secure storage, consider them logged in
        return secureStorage.getUsername() != "Learner"
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            if (username.isBlank() || password.isBlank()) {
                _state.value = AuthState.Error("Username and password cannot be empty.")
                return@launch
            }

            val user = userDao.getUserByUsername(username)
            if (user == null) {
                _state.value = AuthState.Error("Username not found.")
                return@launch
            }

            val valid = PasswordHasher.verifyPassword(password, user.salt, user.passwordHash)
            if (valid) {
                secureStorage.saveUsername(user.username)
                _state.value = AuthState.Success
            } else {
                _state.value = AuthState.Error("Invalid password.")
            }
        }
    }

    fun signUp(username: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            if (username.isBlank() || password.isBlank()) {
                _state.value = AuthState.Error("Username and password cannot be empty.")
                return@launch
            }

            if (userDao.getUserByUsername(username) != null) {
                _state.value = AuthState.Error("Username is already taken.")
                return@launch
            }

            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword(password, salt)

            val user = UserEntity(
                id = UUID.randomUUID().toString(),
                username = username,
                passwordHash = hash,
                salt = salt,
                createdAt = System.currentTimeMillis()
            )

            try {
                userDao.registerUser(user)
                secureStorage.saveUsername(username)
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Failed to sign up user.")
            }
        }
    }

    fun continueAsGuest() {
        secureStorage.saveUsername("Guest")
        _state.value = AuthState.Success
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
