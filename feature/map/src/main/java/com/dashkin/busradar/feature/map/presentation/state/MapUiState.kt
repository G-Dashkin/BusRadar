package com.dashkin.busradar.feature.map.presentation.state

import com.dashkin.busradar.feature.map.domain.model.BusPosition

// Represents all possible states of the map screen UI.
sealed class MapUiState {

    // Initial load or refresh is in progress.
    data object Loading : MapUiState()

    // Bus positions loaded successfully.
    data class Success(val buses: List<BusPosition>) : MapUiState()

    // Data loaded but no buses are currently active.
    data object Empty : MapUiState()

    // An error occurred during loading.
    data class Error(val message: String) : MapUiState()
}
