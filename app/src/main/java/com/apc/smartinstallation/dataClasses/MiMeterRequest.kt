package com.apc.smartinstallation.dataClasses

data class MiMeterRequest(
    val acct_id: String,
    val organization: String,
    val old_meter_image: String,
    val old_meter_model: String,
    val old_meter_number: String,
    val old_meter_reading: String,
    val old_meter_phase: String,
    val new_meter_image: String,
    val new_meter_model: String,
    val new_meter_number: String,
    val new_meter_reading: String,
    val new_meter_phase: String,
    val new_meter_box_seal: String,
    val new_meter_seal: String,
    val mrn_pdf: String
)