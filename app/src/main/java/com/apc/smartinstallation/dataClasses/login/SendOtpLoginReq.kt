package com.apc.smartinstallation.dataClasses.login

data class SendLoginOtpReq(
    val code: Int,
    val uid: String

)