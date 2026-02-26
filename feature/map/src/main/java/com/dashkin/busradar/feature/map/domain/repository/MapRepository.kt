package com.dashkin.busradar.feature.map.domain.repository

import com.dashkin.busradar.feature.map.domain.model.BusPosition
import com.dashkin.busradar.feature.map.domain.model.Result

// Contract for fetching real-time bus position data
interface MapRepository {
    // Returns the current positions of all active buses.
    // Implementation fetches from the GTFS-RT Vehicle Positions feed.
    suspend fun getBusPositions(): Result<List<BusPosition>>
}
