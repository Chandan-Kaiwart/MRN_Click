package com.apc.smartinstallation.api

import com.apc.smartinstallation.dataClasses.LoginRequest
import com.apc.smartinstallation.dataClasses.LoginResponse
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpRes
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpRes
import com.apc.smartinstallation.dataClasses.mobVer.SendOtpResp
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApi {
    @POST("/auth/mr/login/otp/send")
    @Headers("Content-Type: application/json")
    suspend fun sendLoginOtp(
        @Body sendLoginOtpReq: SendLoginOtpReq
    ): Response<SendLoginOtpRes>

    @POST("/auth/mr/login/otp/verify")
    @Headers("Content-Type: application/json")
    suspend fun verLoginOtp(
        @Body verLoginOtpReq: VerLoginOtpReq
    ): Response<VerLoginOtpRes>


    @GET("/meterreplacement/API/login.php")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<LoginRes>
}