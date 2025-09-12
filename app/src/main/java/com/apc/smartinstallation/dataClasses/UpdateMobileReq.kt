package com.apc.smartinstallation.dataClasses

data class UpdateMobileReq(
    val acctId: String,
    val mobile: String,
    val remarks:String
)