package com.apc.smartinstallation.dataClasses

data class MeterReading(
    val meter_number: String,
    val reading_value: Double,
    val image_url: String,
    val latitude: Double,
    val longitude: Double
)
