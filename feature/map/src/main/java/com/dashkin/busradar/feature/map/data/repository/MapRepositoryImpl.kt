package com.dashkin.busradar.feature.map.data.repository

import com.dashkin.busradar.feature.map.domain.model.BusPosition
import com.dashkin.busradar.feature.map.domain.model.Result
import com.dashkin.busradar.feature.map.domain.repository.MapRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

// Stub implementation of MapRepository.
// Returns a set of hardcoded bus positions near Central London with slight
// random movement on each call to simulate real-time updates.
// Replace this implementation with a real GTFS-RT network call
// once :core:network is wired up.
class MapRepositoryImpl @Inject constructor() : MapRepository {

    override suspend fun getBusPositions(): Result<List<BusPosition>> {
        delay(SIMULATED_NETWORK_DELAY_MS)
        return Result.Success(STUB_BUSES.map { it.withRandomMovement() })
    }

    private fun BusPosition.withRandomMovement(): BusPosition = copy(
        latitude = latitude + (Random.nextDouble() - MOVEMENT_CENTER) * MOVEMENT_RANGE_DEG,
        longitude = longitude + (Random.nextDouble() - MOVEMENT_CENTER) * MOVEMENT_RANGE_DEG,
        bearing = ((bearing ?: 0f) + (Random.nextFloat() - MOVEMENT_CENTER.toFloat()) * BEARING_DRIFT_DEG) % BEARING_MAX,
        timestamp = System.currentTimeMillis(),
    )

    private companion object {
        const val SIMULATED_NETWORK_DELAY_MS = 300L
        const val MOVEMENT_CENTER = 0.5
        const val MOVEMENT_RANGE_DEG = 0.002
        const val BEARING_DRIFT_DEG = 10f
        const val BEARING_MAX = 360f

        val STUB_BUSES = listOf(
            BusPosition(
                vehicleId = "VH001",
                routeId = "25",
                tripId = null,
                latitude = 51.5152,
                longitude = -0.0879,
                bearing = 90f,
                speed = 8.3f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "TFLO",
                currentStopSequence = 4,
            ),
            BusPosition(
                vehicleId = "VH002",
                routeId = "73",
                tripId = null,
                latitude = 51.5194,
                longitude = -0.1270,
                bearing = 180f,
                speed = 6.1f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "TFLO",
                currentStopSequence = 7,
            ),
            BusPosition(
                vehicleId = "VH003",
                routeId = "11",
                tripId = null,
                latitude = 51.5033,
                longitude = -0.1195,
                bearing = 270f,
                speed = 10.5f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "TFLO",
                currentStopSequence = 2,
            ),
            BusPosition(
                vehicleId = "VH004",
                routeId = "15",
                tripId = null,
                latitude = 51.5074,
                longitude = -0.1020,
                bearing = 45f,
                speed = 5.2f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "ARRIVA",
                currentStopSequence = 9,
            ),
            BusPosition(
                vehicleId = "VH005",
                routeId = "9",
                tripId = null,
                latitude = 51.5000,
                longitude = -0.1425,
                bearing = 135f,
                speed = 7.8f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "GOAHEAD",
                currentStopSequence = 1,
            ),
            BusPosition(
                vehicleId = "VH006",
                routeId = "RV1",
                tripId = null,
                latitude = 51.5085,
                longitude = -0.0754,
                bearing = 315f,
                speed = 9.0f,
                timestamp = System.currentTimeMillis(),
                operatorRef = "ARRIVA",
                currentStopSequence = 5,
            ),
        )
    }
}
