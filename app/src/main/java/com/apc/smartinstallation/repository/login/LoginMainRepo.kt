package com.apc.smartinstallation.repository.login
import com.apc.smartinstallation.dataClasses.LoginRequest
import com.apc.smartinstallation.dataClasses.LoginResponse
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpRes
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpRes
import com.apc.smartinstallation.util.Resource


interface LoginMainRepo {
    suspend fun sendLoginOtp(sendLoginOtpReq: SendLoginOtpReq): Resource<SendLoginOtpRes>
    suspend fun verLoginOtp(verLoginOtpReq: VerLoginOtpReq): Resource<VerLoginOtpRes>
    suspend fun loginUser(username: String, password: String): Resource<LoginRes>


}