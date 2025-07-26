package com.apc.smartinstallation.helper

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class ImageUploadHelper(private val context: Context) {

    private val client = OkHttpClient()
    private val apiKey = "x7y8z9a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2"
    private val uploadUrl = "http://192.168.110.39/img_upload_api.php"
    suspend fun uploadImage(imageFile: File, meterId: String, location: String): Result<Unit> {
        return try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("meter_id", meterId)
                .addFormDataPart("location", location)
                .addFormDataPart(
                    "captured_image",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("X-API-KEY", apiKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                val errorCode = response.code
                val errorMsg = response.body?.string() ?: "No error message from server"
                Result.Failure(IOException("HTTP $errorCode: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}