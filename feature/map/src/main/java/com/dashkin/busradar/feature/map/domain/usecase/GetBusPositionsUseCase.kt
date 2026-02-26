package com.dashkin.busradar.feature.map.domain.usecase

import com.dashkin.busradar.feature.map.domain.model.BusPosition
import com.dashkin.busradar.feature.map.domain.model.Result
import com.dashkin.busradar.feature.map.domain.repository.MapRepository
import javax.inject.Inject

// Fetches the current positions of all active buses.
// Single-responsibility use case that delegates to [MapRepository]
// and can be extended with filtering or sorting logic without touching the ViewModel.
class GetBusPositionsUseCase @Inject constructor(
    private val repository: MapRepository,
) {
    suspend operator fun invoke(): Result<List<BusPosition>> = repository.getBusPositions()
}
