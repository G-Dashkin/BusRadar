package com.dashkin.busradar.feature.map.domain.model

data class BusPosition(
    val vehicleId: String, // Unique identifier for the vehicle (from GTFS-RT VehicleDescriptor)
    val routeId: String, // Route identifier (e.g. "25", "73")
    val tripId: String?, // Optional trip identifier for linking to trip updates
    val latitude: Double, // WGS84 latitude
    val longitude: Double, // WGS84 longitude
    val bearing: Float?, // Direction of travel in degrees (0–360, clockwise from North). Null if unknown.
    val speed: Float?, // Current speed in m/s. Null if unknown.
    val timestamp: Long, // Unix timestamp (ms) of the last position update.
    val operatorRef: String?, // Optional operator code (e.g. "TFLO").
    val currentStopSequence: Int?, // Index of the current stop within the trip. Null if unknown.
)
