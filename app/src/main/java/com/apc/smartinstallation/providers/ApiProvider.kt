package com.apc.smartinstallation.providers

import com.apc.smartinstallation.api.GeoApi
import com.apc.smartinstallation.api.HomeApi
import com.apc.smartinstallation.api.LoginApi
import com.apc.smartinstallation.api.MobVerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {

    private const val BASE_URL = "http://147.93.102.121/meterreplacement/API/"
    private const val GEO_BASE_URL = "https://maps.googleapis.com"
    private const val MOB_VER_BASE_URL = "https://www.mysmsapp.in"






    @Singleton
    @Provides
    fun provideOkHttpClient(
    ): OkHttpClient {
        val interceptor =  HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .readTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideLoginApi(retrofit: Retrofit): LoginApi {
        return retrofit.create(LoginApi::class.java)
    }

    @Singleton
    @Provides
    fun provideHomeApi(retrofit: Retrofit): HomeApi {
        return retrofit.create(HomeApi::class.java)
    }

    @Singleton
    @Provides
    fun provideGeoApi(okHttpClient: OkHttpClient): GeoApi {
        return Retrofit.Builder()
            .baseUrl(GEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(GeoApi::class.java)
    }

    @Singleton
    @Provides
    fun provideMobVerApi(okHttpClient: OkHttpClient): MobVerApi {
        return Retrofit.Builder()
            .baseUrl(MOB_VER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(MobVerApi::class.java)
    }
}
