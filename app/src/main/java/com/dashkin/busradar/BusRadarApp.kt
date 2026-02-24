package com.dashkin.busradar

import android.app.Application
import com.dashkin.busradar.di.AppComponent
import com.dashkin.busradar.di.DaggerAppComponent

class BusRadarApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}
