package com.apc.smartinstallation.providers

import com.apc.smartinstallation.api.GeoApi
import com.apc.smartinstallation.api.HomeApi
import com.apc.smartinstallation.dispatchers.DispatcherTypes
import com.apc.smartinstallation.repository.home.HomeDefRepo
import com.apc.smartinstallation.repository.home.HomeMainRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvider {


    @Singleton
    @Provides
    fun provideHomeRepository(api: HomeApi,geoApi: GeoApi, dispatcherProvider: DispatcherTypes): HomeMainRepo =
        HomeDefRepo(api,geoApi, dispatcherProvider)
}