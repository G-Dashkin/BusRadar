package com.dashkin.busradar

import android.app.Application
import com.dashkin.busradar.di.AppComponent
import com.dashkin.busradar.di.DaggerAppComponent
import com.dashkin.busradar.feature.map.di.MapComponentProvider
import com.dashkin.busradar.feature.map.presentation.fragment.MapFragment

class BusRadarApp : Application(), MapComponentProvider {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    override fun injectMapFragment(fragment: MapFragment) = appComponent.injectMapFragment(fragment)
}
