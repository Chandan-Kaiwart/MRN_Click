package com.apc.smartinstallation.api

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MeterData?,
    val error: Boolean = false
)

data class MeterData(
    @SerializedName("id") val id: Int?,
    @SerializedName("meter_number") val meterNumber: String?,
    @SerializedName("reading_value") val readingValue: String?,
    @SerializedName("timestamp") val timestamp: String?
)