package com.togalugombe.aiguide.ui.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.togalugombe.aiguide.data.model.Artist
import com.togalugombe.aiguide.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ArtistsViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Artists List State
    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    // Error State
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Load Artists
    fun fetchArtists() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val list = repository.getArtists()
                _artists.postValue(list)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load artisans")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
