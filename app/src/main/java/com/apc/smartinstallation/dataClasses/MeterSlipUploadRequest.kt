package com.apc.smartinstallation.dataClasses

data class MeterSlipUploadRequest(
    val consumerId: String,
    val meterNumber: String,
    val imageBase64: String,
    val timestamp: String
)