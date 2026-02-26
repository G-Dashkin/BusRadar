package com.dashkin.busradar.feature.map.domain.model

// Wrapper for domain-layer operation results.
// Named Outcome to avoid shadowing kotlin.Result from the standard library.
// Intended to be moved to :core:utils once that module is created.
sealed class Outcome<out T> {

    data class Success<T>(val data: T) : Outcome<T>()

    data class Error(val message: String, val cause: Throwable? = null) : Outcome<Nothing>()
}
