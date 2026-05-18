package com.togalugombe.aiguide.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.togalugombe.aiguide.data.model.User
import com.togalugombe.aiguide.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Current User State
    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> get() = _userProfile

    // Logout Success State
    private val _isLoggedOut = MutableLiveData<Boolean>(false)
    val isLoggedOut: LiveData<Boolean> get() = _isLoggedOut

    // Error State
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Fetch User Details from Repository
    fun fetchUserProfile() {
        val currentUser = repository.getCurrentUser()
        if (currentUser == null) {
            _errorMessage.value = "User is not logged in"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                val details = repository.getUserDetails(currentUser.uid)
                _userProfile.postValue(details)
            } catch (e: Exception) {
                // Fallback to Auth information directly if firestore details aren't loaded yet
                val fallbackUser = User(
                    uid = currentUser.uid,
                    name = currentUser.displayName ?: "Cultural App User",
                    email = currentUser.email ?: ""
                )
                _userProfile.postValue(fallbackUser)
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load detailed profile details")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Logout Action
    fun logout() {
        repository.logoutUser()
        _isLoggedOut.value = true
    }
}
