package com.dashkin.busradar.feature.map.domain.model

// Intended to be moved to :core:utils once that module is created
sealed class Result<out T> {

    // Operation completed successfully with data
    data class Success<T>(val data: T) : Result<T>()

    // Operation failed with a human-readable [message] and optional [cause].
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
