package com.apc.smartinstallation.repository.login

import android.util.Log
import com.apc.smartinstallation.api.LoginApi
import com.apc.smartinstallation.dataClasses.LoginRequest
import com.apc.smartinstallation.dataClasses.LoginResponse
import com.apc.smartinstallation.dataClasses.login.LoginRes
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.SendLoginOtpRes
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpReq
import com.apc.smartinstallation.dataClasses.login.VerLoginOtpRes
import com.apc.smartinstallation.dispatchers.DispatcherTypes
import com.apc.smartinstallation.util.Resource
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginDefRepo @Inject constructor(
    private val apiService: LoginApi,
    private val dispatcherProvider: DispatcherTypes
) : LoginMainRepo {



    override suspend fun sendLoginOtp(
        sendLoginOtpReq: SendLoginOtpReq
    ): Resource<SendLoginOtpRes> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.sendLoginOtp(sendLoginOtpReq)
            val result = response.body()
            if (response.isSuccessful && result != null) {


                    Resource.Success(result)



            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                Resource.Error(result?.message)

            }
        } catch (e: IOException) {
            // Network issue, use local database

            Resource.Error(e.message)

        }
    }


    override suspend fun verLoginOtp(
        verLoginOtpReq: VerLoginOtpReq
    ): Resource<VerLoginOtpRes> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.verLoginOtp(verLoginOtpReq)
            val result = response.body()
            if (response.isSuccessful && result != null) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")


                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            Resource.Error(e.message)

        }
    }
    override suspend fun loginUser(
        username: String,
        password: String
    ): Resource<LoginRes> = withContext(dispatcherProvider.io) {
        try {
            // Attempt to login with the remote server
            val response = apiService.loginUser(username, password)
            val result = response.body()
            if (response.isSuccessful && result != null && !result.error) {
                Log.d("RETRO>>", response.code().toString())
                Resource.Success(result)
            } else {

                // On failure, fallback to local database
                Log.d("RETRO>>", "Error")

                Resource.Error(response.message())

            }
        } catch (e: IOException) {
            // Network issue, use local database
            Resource.Error(e.message)

        }
    }

}