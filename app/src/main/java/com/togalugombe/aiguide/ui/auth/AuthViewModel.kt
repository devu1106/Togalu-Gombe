package com.togalugombe.aiguide.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.togalugombe.aiguide.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Operation Success Status
    private val _userState = MutableLiveData<FirebaseUser?>()
    val userState: LiveData<FirebaseUser?> get() = _userState

    // Error Message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // check current logged-in session on launch
    fun checkUserSession() {
        _userState.value = repository.getCurrentUser()
    }

    // 1. LOGIN ACTION
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill in all credentials fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                _userState.postValue(user)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Sign in failed")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // 2. REGISTER ACTION
    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "All registry fields are mandatory"
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val user = repository.registerUser(name, email, password)
                _userState.postValue(user)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Account creation failed")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Reset error trigger
    fun clearError() {
        _errorMessage.value = null
    }
}
