package com.apc.smartinstallation.api

//data class MeterReadingRequest(
//    val meter_number: String,
//    val meter_make: String,
//    val reading_value: String,
//    val image_path: String,
//    val latitude: String,
//    val longitude: String,
//    val meter_type: String,

//    val consumer_mobile_no: String? = null,
//    val box_seal: String? = null,
//    val body_seal: String? = null,
//    val terminal_seal: String? = null
//
//)
data class MeterReadingRequest(
    val meter_type: String,
    val meter_number: String,
    val meter_make: String,
    val reading_value: String,
    val image_path: String,
    val consumer_mobile_no: String? = null,
    val box_seal: String? = null,
    val body_seal: String? = null,
    val terminal_seal: String? = null,
    val latitude: String,
    val longitude: String,
    val old_meter_no: String? = null,
    val old_meter_reading: String? = null,
    val old_meter_make: String? = null,
    val old_phase: Int? = null,
    val phase: Int? = null,
    val account_id: String? = null,
    val manual_reading: String? = null,
    val condition: String? = null,
    val manualReading: String = "",
    val consumer_name: String? = null,
    val consumer_no: String? = null,
)