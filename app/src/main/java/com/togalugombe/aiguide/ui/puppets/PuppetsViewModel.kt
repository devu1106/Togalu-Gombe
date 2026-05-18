package com.togalugombe.aiguide.ui.puppets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.togalugombe.aiguide.data.model.Puppet
import com.togalugombe.aiguide.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class PuppetsViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Loading State
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Puppets List State
    private val _puppets = MutableLiveData<List<Puppet>>()
    val puppets: LiveData<List<Puppet>> get() = _puppets

    // Selected Puppet Detail State
    private val _selectedPuppet = MutableLiveData<Puppet>()
    val selectedPuppet: LiveData<Puppet> get() = _selectedPuppet

    // Error State
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // Load All Puppets
    fun fetchPuppets() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val list = repository.getPuppets()
                _puppets.postValue(list)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load puppets catalog")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Load Puppet Details
    fun fetchPuppetDetails(puppetId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val details = repository.getPuppetDetails(puppetId)
                _selectedPuppet.postValue(details)
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to load puppet spec details")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
