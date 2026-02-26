package com.dashkin.busradar.feature.map.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dashkin.busradar.feature.map.domain.usecase.GetBusPositionsUseCase
import javax.inject.Inject

// Factory for creating MapViewModel with its injected dependencies.
class MapViewModelFactory @Inject constructor(
    private val getBusPositionsUseCase: GetBusPositionsUseCase,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MapViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return MapViewModel(getBusPositionsUseCase) as T
    }
}
