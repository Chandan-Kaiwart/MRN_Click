package com.apc.smartinstallation.dataClasses

import com.google.gson.annotations.SerializedName

data class CiMeterRequest(
    @SerializedName("acct_id") val acct_id: String,
    @SerializedName("organization") val organization: String,
    @SerializedName("old_meter_image") val old_meter_image: String,
    @SerializedName("old_meter_number") val old_meter_number: String,
    @SerializedName("old_meter_model") val old_meter_model: String,
    @SerializedName("old_meter_reading") val old_meter_reading: String,
    @SerializedName("old_meter_parameter") val old_meter_parameter: String,
    @SerializedName("old_meter_phase") val old_meter_phase: String
)