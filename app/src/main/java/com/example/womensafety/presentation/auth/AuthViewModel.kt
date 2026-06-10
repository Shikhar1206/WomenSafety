package com.example.womensafety.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.womensafety.core.util.Resource
import com.example.womensafety.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = authRepository.currentUser
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.isLoggedIn())

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signIn(email, password)) {
                is Resource.Success -> _uiState.value = AuthUiState(success = true)
                is Resource.Error -> _uiState.value = AuthUiState(error = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.signUp(email, password, name)) {
                is Resource.Success -> _uiState.value = AuthUiState(success = true)
                is Resource.Error -> _uiState.value = AuthUiState(error = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = authRepository.sendPasswordReset(email)) {
                is Resource.Success -> _uiState.value = AuthUiState(success = true)
                is Resource.Error -> _uiState.value = AuthUiState(error = result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
