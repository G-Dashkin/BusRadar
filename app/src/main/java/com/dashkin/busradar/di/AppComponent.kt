package com.dashkin.busradar.di

import android.app.Application
import com.dashkin.busradar.MainActivity
import com.dashkin.busradar.feature.map.di.MapComponentProvider
import com.dashkin.busradar.feature.map.di.MapModule
import com.dashkin.busradar.feature.map.presentation.fragment.MapFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, MapModule::class])
interface AppComponent : MapComponentProvider {

    fun inject(activity: MainActivity)

    override fun injectMapFragment(fragment: MapFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}
