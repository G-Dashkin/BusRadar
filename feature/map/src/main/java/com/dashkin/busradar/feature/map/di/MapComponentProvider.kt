package com.dashkin.busradar.feature.map.di

import com.dashkin.busradar.feature.map.presentation.fragment.MapFragment

// Interface implemented by the Application class to inject MapFragment.
// Feature modules must not depend on :app, so MapFragment casts
// `requireActivity().application` to this interface instead of referencing
// BusRadarApp directly.
interface MapComponentProvider {
    fun injectMapFragment(fragment: MapFragment)
}
