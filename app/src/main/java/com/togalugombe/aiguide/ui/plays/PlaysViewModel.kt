package com.togalugombe.aiguide.ui.plays

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.togalugombe.aiguide.data.model.Play
import com.togalugombe.aiguide.data.model.Scene
import com.togalugombe.aiguide.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class PlaysViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Plays List State
    private val _plays = MutableLiveData<List<Play>>()
    val plays: LiveData<List<Play>> get() = _plays

    // Scenes List State
    private val _scenes = MutableLiveData<List<Scene>>()
    val scenes: LiveData<List<Scene>> get() = _scenes

    // Selected Scene Detail State
    private val _selectedScene = MutableLiveData<Scene>()
    val selectedScene: LiveData<Scene> get() = _selectedScene

    // Error State
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Load Plays
    fun fetchPlays() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val list = repository.getPlays()
                _plays.postValue(list)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load plays")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Load Scenes for Play ID
    fun fetchScenesForPlay(playId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val list = repository.getScenesForPlay(playId)
                _scenes.postValue(list)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load scenes")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Load Scene Details by Scene ID
    fun fetchSceneDetails(sceneId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val details = repository.getSceneDetails(sceneId)
                _selectedScene.postValue(details)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load scene details")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
