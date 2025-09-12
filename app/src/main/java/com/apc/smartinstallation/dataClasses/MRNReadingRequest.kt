package com.apc.smartinstallation.dataClasses

data class MRNReadingRequest(
    val image_name: String,
    val image_path: String,
    val reading: String,  // Changed from reading_value to match API
    val consumer_name: String,
    val consumer_no: String,
    val consumer_mobile_no: String?,
    val box_seal: String?,
    val body_seal: String?,
    val terminal_seal: String?,
    val old_meter_no: String?,
    val old_meter_reading: String?,
    val old_meter_make: String?,
    val old_phase: String?,  // Changed from Int to String to match API
    val meter_make: String?,
    val phase: String?,      // Changed from Int to String to match API
    val latitude: String,
    val longitude: String,
    val account_id: String?,
    val manual_reading: String?
)