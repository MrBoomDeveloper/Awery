package com.mrboomdev.awery.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.data.AweryServer
import com.mrboomdev.awery.data.AweryServerNotification
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.sdk.Results
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NotificationsState {
    data object Loading: NotificationsState
    
    data class Error(
        val throwable: Throwable
    ): NotificationsState
    
    data class Loaded(
        val items: List<AweryServerNotification>,
        val loadMore: (() -> Unit)?
    ): NotificationsState
}

class NotificationsViewModel: ViewModel() {
    private var job: Job? = null
    
    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val state = _state.asStateFlow()
    
    private val _isReloading = MutableStateFlow(false)
    val isReloading = _isReloading.asStateFlow()
    
    init {
        job = viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
            Log.e("NotificationsViewModel", "Failed to fetch notifications!", it)
            
            viewModelScope.launch { 
                _state.emit(NotificationsState.Error(it))
            }
        }) {
            load()
        }
    }
    
    fun reload() {
        job?.cancel()
        
        job = viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
            Log.e("NotificationsViewModel", "Failed to fetch notifications!", it)
            
            viewModelScope.launch {
                _state.emit(NotificationsState.Error(it))
                _isReloading.emit(false)
            }
        }) {
            _isReloading.emit(true)
            load()
            _isReloading.emit(false)
        }
    }
    
    private suspend fun load() {
        _state.emit(NotificationsState.Loading)
        
        val loaded = AweryServer.getNotifications(
            AwerySettings.aweryServerToken.value, 0)
        
        _state.emit(NotificationsState.Loaded(loaded.items, null))
    }
}