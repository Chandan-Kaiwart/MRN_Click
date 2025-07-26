package com.apc.smartinstallation.dataClasses;

data class FieldVisitRequest(
        val acctId: String,
        val mobile: String,
        val alternateNo: String,
        val remarks: String,
        val visitDatetime: String,
        val visitedPremise: String,
        val latitude: String,
        val longitude: String,
        val address: String,
        val status: String,
        val distance: String,
        val paidAmount: String
)
