package com.dashkin.busradar.feature.map.di

import com.dashkin.busradar.feature.map.data.repository.MapRepositoryImpl
import com.dashkin.busradar.feature.map.domain.repository.MapRepository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class MapModule {

    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryImpl): MapRepository
}
