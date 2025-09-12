package com.apc.smartinstallation.api

import com.apc.smartinstallation.dataClasses.mobVer.SendOtpResp
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MobVerApi {
    @GET("/api/push.json")
    suspend fun sendOtp(
        @Query("apikey") apiKey: String,
        @Query("sender") address: String,
        @Query("mobileno") mob: String,
        @Query("text") text: String
    ): Response<SendOtpResp>


}