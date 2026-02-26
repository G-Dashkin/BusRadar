package com.dashkin.busradar.feature.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashkin.busradar.feature.map.domain.model.Result
import com.dashkin.busradar.feature.map.domain.usecase.GetBusPositionsUseCase
import com.dashkin.busradar.feature.map.presentation.state.MapUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// Polls GetBusPositionsUseCase every POLLING_INTERVAL_MS milliseconds
// and exposes the result as uiState.
class MapViewModel @Inject constructor(
    private val getBusPositionsUseCase: GetBusPositionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    // Restarts the polling loop (e.g. after a user-triggered retry).
    fun retry() {
        _uiState.value = MapUiState.Loading
        pollingJob?.cancel()
        startPolling()
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchPositions()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    private suspend fun fetchPositions() {
        when (val result = getBusPositionsUseCase()) {
            is Result.Success -> {
                _uiState.value = if (result.data.isEmpty()) MapUiState.Empty
                else MapUiState.Success(result.data)
            }
            is Result.Error -> _uiState.value = MapUiState.Error(result.message)
        }
    }

    private companion object {
        const val POLLING_INTERVAL_MS = 10_000L
    }
}
